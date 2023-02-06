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
    /**
     * start play video
     */
    fun start()

    /**
     * pause video
     */
    fun pause()

    /**
     * get video total time
     *
     * @return total time
     */
    val duration: Int

    /**
     * get video current position
     *
     * @return current position
     */
    val currentPosition: Int

    /**
     * seek video to exactly position
     *
     * @param position position
     */
    fun seekTo(position: Int)

    /**
     * video is playing state
     *
     * @return is video playing
     */
    val isPlaying: Boolean

    fun startMedia(file: File)

    fun stop()

    fun attachVlcVideoView(vlcVideoLayout: VLCVideoLayout)

    fun playNext()

    fun playPrevious()

    fun detachViews()

    val currentMediaFile: File?

    fun bind(vlcPlayerFragment: VlcPlayerFragment)

    fun createMedia(file: File): Media

    fun getDuration(file:File) :Long
}