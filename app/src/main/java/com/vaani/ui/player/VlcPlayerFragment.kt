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
import kotlin.math.log


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

        controller =
            VideoControllerView(requireActivity(),vlcVideoLayout.findViewById(org.videolan.R.id.surface_video),vlcVideoLayout, playerInterface)
    }

    private fun updateCurrentPlayback(file: File) {
        currentPlayBack?.let {
            if (it.file == file) {
                return
            }
            it.mediaPlayer.release()
        }
        val vlc = LibVLC(requireContext())
        val mediaPlayer = MediaPlayer(vlc)
        mediaPlayer.media = createMedia(file, vlc)
        mediaPlayer.play()
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

    private val playerInterface = object : VideoControllerView.MediaPlayerControlListener {
        override fun start() {
            mediaPlayer.play()
        }

        override fun pause() {
            mediaPlayer.pause()
        }

        override fun getDuration(): Int {
            return mediaPlayer.length.toInt()
        }

        override fun getCurrentPosition(): Int {
            return (mediaPlayer.position * duration).toInt()
        }

        override fun seekTo(pos: Int) {
            mediaPlayer.position = pos.toFloat() / duration
        }

        override fun isPlaying(): Boolean {
            return mediaPlayer.isPlaying
        }

        override fun isComplete(): Boolean {
            return currentPosition == duration
        }

        override fun getBufferPercentage(): Int {
            return 0
        }

        override fun isFullScreen(): Boolean {
            return requireActivity().requestedOrientation == ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        }

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

    }
}
