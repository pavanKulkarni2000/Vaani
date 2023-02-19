package com.vaani.ui.player

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import androidx.fragment.app.Fragment
import com.vaani.MainActivity
import com.vaani.R
import com.vaani.models.File
import com.vaani.util.TAG
import org.videolan.libvlc.util.VLCVideoLayout


class VlcPlayerFragment( var file:File ) :
    Fragment(R.layout.vlc_player_layout), PlayerViewListener {
    private lateinit var surfaceView: SurfaceView
    private lateinit var videoControllerView: VideoControllerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val vlcVideoLayout: VLCVideoLayout = view.findViewById(R.id.video_layout)
        Player.mediaPlayerService.apply {
            attachVlcVideoView(vlcVideoLayout,this@VlcPlayerFragment)
            startNewMedia(file)
        }
        surfaceView = vlcVideoLayout.findViewById(org.videolan.R.id.surface_video)
        if (file.isAudio) {
            setImage(file)
        }
        videoControllerView = VideoControllerView(
            requireActivity(),
            surfaceView,
            vlcVideoLayout,
            this
        )
    }

    private fun setImage(file: File) {
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

    private fun getImageBitmap(file: File): Bitmap {

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

    override val isFullScreen: Boolean
        get() = requireActivity().requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

    override fun toggleFullScreen() {
        if (isFullScreen) {
            requireActivity().requestedOrientation =
                ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }
    }

    override fun exit() {
        parentFragmentManager.popBackStack()
    }


    override fun onDestroy() {
        super.onDestroy()
        Player.mediaPlayerService.detachViews()
        videoControllerView.exit()
    }

    override fun mediaChanged(file: File) {
        this.file = file
        videoControllerView.mediaChanged()
        if(file.isAudio) {
            setImage(file)
        }
    }
}
