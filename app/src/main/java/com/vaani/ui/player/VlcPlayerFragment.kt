package com.vaani.ui.player

import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.vaani.MainActivity
import com.vaani.R
import com.vaani.models.File
import com.vaani.models.PlayBack
import com.vaani.util.TAG
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout


class VlcPlayerFragment(private val file: File?) :
    Fragment(R.layout.vlc_player_layout) {

    companion object {
        var currentPlayBack: PlayBack = PlayBack(null,null,null)
    }

    private lateinit var vlcVideoLayout: VLCVideoLayout
    private lateinit var controller: VideoControllerView
    private lateinit var mediaPlayer: MediaPlayer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // file not passed if not playing new media
        if (file != null) {
            updateCurrentPlayback(file)
        } else if (currentPlayBack.file == null) {
            // no file, no existing playback info
            Toast.makeText(requireContext(), "Nothing to play", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        mediaPlayer = currentPlayBack.mediaPlayer!!

        vlcVideoLayout = view.findViewById(R.id.video_layout)

        mediaPlayer.attachViews(vlcVideoLayout, null, false, false)

        mediaPlayer.setEventListener(vlcPlayerListener)

        val surfaceView: SurfaceView = vlcVideoLayout.findViewById(org.videolan.R.id.surface_video)

        if(currentPlayBack.file?.isAudio == true){
            MainActivity.context.assets.run {
                val imageName = list("player_images")!!.random()
                open("player_images/$imageName").use {
                    surfaceView.holder.addCallback(object : SurfaceHolder.Callback{
                        val bitmap = BitmapFactory.decodeStream(it)
                        override fun surfaceCreated(holder: SurfaceHolder) {
                            val canvas = holder.lockCanvas()
                            val scaleFactor =
                                Math.min(canvas.width.toFloat() / bitmap.width, canvas.height.toFloat() / bitmap.height)
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

                        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                            val canvas: Canvas = holder.lockCanvas()
                            val scaleFactor =
                                Math.min(canvas.width.toFloat() / bitmap.width, canvas.height.toFloat() / bitmap.height)
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

                        override fun surfaceDestroyed(holder: SurfaceHolder) {
                            bitmap.recycle()
                        }

                    })
                }
            }
        }

        controller =
            VideoControllerView(
                requireActivity(),
                surfaceView,
                vlcVideoLayout,
                playerInterface
            )
    }

    private fun updateCurrentPlayback(file: File) {
        currentPlayBack.file?.let {
            if (it == file) {
                currentPlayBack.mediaPlayer?.play()
                return
            }
            currentPlayBack.mediaPlayer?.release()
        }
        val vlc = LibVLC(requireContext().applicationContext)
        val mediaPlayer = MediaPlayer(vlc)
        mediaPlayer.media = createMedia(file, vlc)
        mediaPlayer.play()
        Log.d(TAG, "updateCurrentPlayback: ${mediaPlayer.position}")
        currentPlayBack.let {
            it.vlc = vlc
            it.file=file
            it.mediaPlayer=mediaPlayer
        }
    }

    private fun createMedia(file: File, vlc: LibVLC): Media? {
        return if (file.isUri) {
            val parcelFileDescriptor =
                requireContext().contentResolver.openFileDescriptor(Uri.parse(file.path), "r")
            if (parcelFileDescriptor == null) {
                Toast.makeText(
                    requireContext(),
                    "Media can't be played",
                    Toast.LENGTH_SHORT
                ).show()
                parentFragmentManager.popBackStack()
                return null
            }
            val media = Media(vlc, parcelFileDescriptor.fileDescriptor)
            parcelFileDescriptor.detachFd()
            parcelFileDescriptor.close()
            media
        } else {
            Media(vlc, file.path)
        }
    }

    private fun finishPlayer() {
        controller.exit()
        currentPlayBack.file?.let {
            currentPlayBack.vlc?.release()
            currentPlayBack.mediaPlayer?.apply {
                stop()
                release()
            }
            currentPlayBack.let {
                it.vlc=null
                it.mediaPlayer=null
                it.file=null
            }
        }
        parentFragmentManager.popBackStack()
    }

    private val vlcPlayerListener = MediaPlayer.EventListener{
        event ->
        when(event.type){
            MediaPlayer.Event.Playing, MediaPlayer.Event.EndReached, MediaPlayer.Event.Paused, MediaPlayer.Event.Stopped -> {}
        }
    }

    private val playerInterface = object : VideoControllerView.MediaPlayerControlListener {
        override fun start() {
            mediaPlayer.play()
        }

        override fun pause() {
            mediaPlayer.pause()
        }

        override val duration: Int
            get() =  mediaPlayer.length.toInt()

        override val currentPosition: Int
            get() = (mediaPlayer.position * duration).toInt()

        override fun seekTo(position: Int) {
            mediaPlayer.position = position.toFloat() / duration
        }

        override val isPlaying: Boolean
            get() = mediaPlayer.isPlaying

        override val isComplete: Boolean
            get() = currentPosition == duration

        override val bufferPercentage: Int
            get() = 0

        override val isFullScreen: Boolean
            get() =  requireActivity().requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        override fun toggleFullScreen() {
            if (isFullScreen) {
                requireActivity().requestedOrientation =
                    ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            } else {
                requireActivity().requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
        }

        override fun exit() {
            finishPlayer()
        }

    }
}
