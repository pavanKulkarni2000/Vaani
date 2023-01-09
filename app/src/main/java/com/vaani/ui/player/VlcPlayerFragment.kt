package com.vaani.ui.player

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
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
        var currentPlayBack: PlayBack? = null
    }

    private lateinit var vlcVideoLayout: VLCVideoLayout
    private lateinit var controller: VideoControllerView
    private lateinit var mediaPlayer: MediaPlayer

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // file not passed if not playing new media
        if (file != null) {
            updateCurrentPlayback(file)
        } else if (currentPlayBack == null) {
            // no file, no existing playback info
            Toast.makeText(requireContext(), "Nothing to play", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        mediaPlayer = currentPlayBack!!.mediaPlayer

        vlcVideoLayout = view.findViewById(R.id.video_layout)

        mediaPlayer.attachViews(vlcVideoLayout, null, false, false)

        mediaPlayer.setEventListener(vlcPlayerListener)

        controller =
            VideoControllerView(
                requireActivity(),
                vlcVideoLayout.findViewById(org.videolan.R.id.surface_video),
                vlcVideoLayout,
                playerInterface
            )
    }

    private fun updateCurrentPlayback(file: File) {
        currentPlayBack?.let {
            if (it.file == file) {
                it.mediaPlayer.play()
                return
            }
            it.mediaPlayer.release()
        }
        val vlc = LibVLC(requireContext().applicationContext)
        val mediaPlayer = MediaPlayer(vlc)
        mediaPlayer.media = createMedia(file, vlc)
        mediaPlayer.play()
        Log.d(TAG, "updateCurrentPlayback: ${mediaPlayer.position}")
        currentPlayBack = PlayBack(mediaPlayer, file, vlc)
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
            currentPlayBack?.let {
                it.vlc.compiler()
                it.mediaPlayer.apply {
                    stop()
                    release()
                }
                currentPlayBack = null
            }
            parentFragmentManager.popBackStack()
        }

    }
}
