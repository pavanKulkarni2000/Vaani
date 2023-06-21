package com.vaani.player

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.SurfaceHolder
import androidx.core.content.ContextCompat
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.vaani.MainActivity
import com.vaani.data.Files
import com.vaani.data.PlayerState
import com.vaani.db.DB
import com.vaani.models.FileEntity
import com.vaani.util.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

object Player {
    private lateinit var browserFuture: ListenableFuture<MediaBrowser>
    private val browser: MediaBrowser?
        get() = if (browserFuture.isDone && !browserFuture.isCancelled) browserFuture.get() else null


    var mediaPlayer= M

    val currentPosition
        get() = mediaPlayerService.currentPosition

    val duration
        get() = mediaPlayerService.duration

    fun init() {
        initializeBrowser()
    }

    private fun initializeBrowser() {
        browserFuture =
            MediaBrowser.Builder(
                this,
                SessionToken(this, ComponentName(this, PlaybackService::class.java))
            )
                .buildAsync()
        browserFuture.addListener({ pushRoot() }, ContextCompat.getMainExecutor(this))
    }

    fun setPlayerService(medPlayerService: PlayerService) {
        mediaPlayerService = medPlayerService
    }

    fun startNewMedia(file: FileEntity) {
        PlayerState.savePreference()
        mediaPlayerService.startMedia(file)
        PlayerState.setCurrentFile(file)
        PlayerState.updatePlaying(true)
    }

    fun attachPlayerView(surfaceHolder: SurfaceHolder) {
        mediaPlayerService.attachView(surfaceHolder)
        PlayerState.setAttached(true)
    }

    fun detachPlayerView() {
        mediaPlayerService.detachViews()
        PlayerState.setAttached(false)
    }

    fun stop() {
        Log.d(TAG, "stop: stopping")
        PlayerState.savePreference()
        mediaPlayerService.stop()
        PlayerState.setCurrentFile(FileEntity())
        PlayerState.updatePlaying(false)
        detachPlayerView()
    }

    fun seekTo(pos: Int) {
        mediaPlayerService.seekTo(pos)
    }

    fun playPrevious() {
        val file = PlayerState.file
        val files = DB.getFolderFiles(file.folderId)
        val prevFile: FileEntity = if (PlayerState.shuffle) {
            files[files.indices.random(PlayBackUtil.random)]
        } else {
            val currIndex = files.indexOf(file)
            if (currIndex == 0) {
                return
            }
            files[currIndex - 1]
        }
        startNewMedia(prevFile)
    }


    fun playNext() {
        val file = PlayerState.file
        if (PlayerState.loop) {
            mediaPlayerService.seekTo(0)
            mediaPlayerService.play()
        } else {
            val files = DB.getFolderFiles(file.folderId)
            val nextFile: FileEntity = if (PlayerState.shuffle) {
                files[files.indices.random(PlayBackUtil.random)]
            } else {
                val currIndex = files.indexOf(file)
                if (currIndex == files.size - 1) {
                    stop()
                    return
                }
                files[currIndex + 1]
            }
            startNewMedia(nextFile)
        }
    }

    fun endReached() {
        val file = PlayerState.file
        mediaPlayerService.seekTo(0)
        playNext()
    }

    fun pause(){
        PlayerState.updatePlaying(true)
        mediaPlayerService.pause()
    }

    fun resume(){
        PlayerState.updatePlaying(false)
        mediaPlayerService.play()
    }

    fun stopLoop() {
        PlayerState.updateLoop(false)
    }

    fun loop() {
        PlayerState.updateLoop(true)
    }

    fun stopShuffle() {
        PlayerState.updateShuffle(false)
    }

    fun shuffle() {
        PlayerState.updateShuffle(true)
    }

    fun updateSpeed(speed: Float) {
        PlayerState.updateSpeed(speed)
        mediaPlayerService.speed = speed
    }

    fun destroy(){
        releaseBrowser()
    }

    private fun releaseBrowser() {
        MediaBrowser.releaseFuture(browserFuture)
    }


}