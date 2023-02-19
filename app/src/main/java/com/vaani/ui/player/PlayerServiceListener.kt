package com.vaani.ui.player

import com.vaani.models.File
import org.videolan.libvlc.Media
import org.videolan.libvlc.util.VLCVideoLayout

/**
 * Interface of Media Controller View Which can be callBack
 * when [android.media.MediaPlayer] or some other media
 * players work
 */
interface PlayerServiceListener {
    val speed: Float

    fun start()

    fun pause()

    val duration: Int

    val currentPosition: Int

    fun seekTo(position: Int)

    val isPlaying: Boolean

    fun startNewMedia(file: File)

    fun stop()

    fun attachVlcVideoView(vlcVideoLayout: VLCVideoLayout, videoListener: PlayerViewListener)

    fun playNext()

    fun playPrevious()

    fun detachViews()

    val currentMediaFile: File?

    fun createMedia(file: File): Media

    fun recallCurrentPlayback()

    fun persistCurrentPlayback()
}