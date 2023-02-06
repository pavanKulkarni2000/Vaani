package com.vaani.ui.player

/**
 * Interface of Media Controller View Which can be callBack
 * when [android.media.MediaPlayer] or some other media
 * players work
 */
interface PlayerViewListener {

    /**
     * video is full screen
     * in order to control image src...
     *
     * @return fullScreen
     */
    val isFullScreen: Boolean

    /**
     * toggle fullScreen
     */
    fun toggleFullScreen()

    /**
     * exit media player
     */
    fun exit()
}