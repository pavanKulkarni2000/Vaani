package com.vaani.ui.player

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.vaani.MainActivity
import com.vaani.R
import com.vaani.data.PlayerState
import com.vaani.models.FileEntity
import com.vaani.player.Player
import com.vaani.util.TAG


class VlcPlayerFragment :
    Fragment(R.layout.vlc_player_layout), SurfaceHolder.Callback {

    private lateinit var surfaceView: SurfaceView
    private lateinit var videoControllerView: VideoControllerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        surfaceView = view.findViewById(R.id.video_layout)
        surfaceView.holder.addCallback(this)
            if (PlayerState.file.isAudio) {
                setImage(PlayerState.file)
            }
        videoControllerView = VideoControllerView(
            requireActivity(),
            surfaceView,
            view.findViewById<FrameLayout>(R.id.video_view_parent)
        )
        PlayerState.fileLive.observe(viewLifecycleOwner) { file ->
            if (file.isAudio) {
                setImage(file)
            }
        }
        PlayerState.isAttachedLive.observe(viewLifecycleOwner) {
            if (!it) {
                Log.d(TAG, "onViewCreated: pop")
                parentFragmentManager.popBackStack()
            }
        }
        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                Log.d(TAG, "handleOnBackPressed: ")
                PlayerState.setAttached(false)
            }
        })
    }

    private fun setImage(file: FileEntity) {
        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            val bitmap = getImageBitmap(file)
            override fun surfaceCreated(holder: SurfaceHolder) {
                val canvas = holder.lockCanvas()
                val scaleFactor =
                    (canvas.width.toFloat() / bitmap.width).coerceAtMost(canvas.height.toFloat() / bitmap.height)
                val scaledBitmap = Bitmap.createScaledBitmap(
                    bitmap,
                    (bitmap.width * scaleFactor).toInt(),
                    (bitmap.height * scaleFactor).toInt(),
                    true
                )

                val x = (canvas.width - scaledBitmap.width).toFloat() / 2
                val y = (canvas.height - scaledBitmap.height).toFloat() / 2
                canvas.drawBitmap(scaledBitmap, x, y, null)
                holder.unlockCanvasAndPost(canvas)
                scaledBitmap.recycle()
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) =
                surfaceCreated(holder)

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                bitmap.recycle()
            }

        })

    }

    private fun getImageBitmap(file: FileEntity): Bitmap {

        try {
            MediaMetadataRetriever().use {
                if (file.isUri) {
                    it.setDataSource(MainActivity.context, Uri.parse(file.path))
                } else {
                    it.setDataSource(file.path)
                }
                it.embeddedPicture?.let { bytes ->
                    return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "makeFile: exception", e)
        }
        MainActivity.context.assets.run {
            val imageName = list("player_images")!!.random()
            open("player_images/$imageName").use {
                return BitmapFactory.decodeStream(it)
            }
        }
    }


    override fun onDestroy() {
        Log.d(TAG, "onDestroy: ")
        super.onDestroy()
        videoControllerView.exit()
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Player.attachPlayerView(holder)
        Log.d(TAG, "surfaceCreated: attached")
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }
}
