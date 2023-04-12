package com.vaani.player

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import com.vaani.db.DB
import com.vaani.models.File
import com.vaani.util.PreferenceUtil
import com.vaani.util.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout


class PlayerService : Service() {

    // Player context
    private lateinit var libVLC: LibVLC
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate() {
        super.onCreate()
        libVLC = LibVLC(applicationContext)
        mediaPlayer = MediaPlayer(libVLC)
        mediaPlayer.setEventListener(eventHandler)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Player.setPlayerService(this)
        Log.d(TAG, "onStartCommand: setting service")
        initObservers()
        return START_STICKY
    }

    private fun initObservers() {
        Player.state.file.observeForever { file ->
            if (file.id > 0) {
                startMedia(file)
            }
        }
        Player.state.speed.observeForever { speed->
            this.speed = speed
        }
    }

    private val eventHandler = MediaPlayer.EventListener { event ->
        when (event.type) {
            MediaPlayer.Event.EndReached -> {
                Player.endReached()
            }
            MediaPlayer.Event.SeekableChanged -> {
                recallCurrentPlayback()
            }
        }
    }

    private fun startMedia(file: File) {
        mediaPlayer.stop()
        val media = createMedia(file)
        mediaPlayer.media = media
        media.release()
        Player.state.updatePlaying(true)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    var speed: Float
        get() = mediaPlayer.rate
        set(value) {
            mediaPlayer.rate = value

        }

    fun play() {
        mediaPlayer.play()
    }

    fun pause() {
        mediaPlayer.pause()
    }

    val duration: Int
        get() = mediaPlayer.length.toInt()

    val currentPosition: Int
        get() = (mediaPlayer.position * duration).toInt()

    fun seekTo(position: Int) {
        mediaPlayer.position = position.toFloat() / duration
    }

    val isPlaying: Boolean
        get() = mediaPlayer.isPlaying

    fun stop() {
        mediaPlayer.stop()
    }

    fun attachVlcVideoView(vlcVideoLayout: VLCVideoLayout) {
        mediaPlayer.attachViews(vlcVideoLayout, null, false, false)
    }


    fun detachViews() {
        mediaPlayer.detachViews()
    }

    private fun createMedia(file: File): Media {
        return if (file.isUri) {
            val parcelFileDescriptor =
                contentResolver.openFileDescriptor(Uri.parse(file.path), "r") ?: throw java.lang.RuntimeException()
            val media = Media(libVLC, parcelFileDescriptor.fileDescriptor)
            parcelFileDescriptor.detachFd()
            parcelFileDescriptor.close()
            media
        } else {
            Media(libVLC, file.path)
        }
    }

    private fun recallCurrentPlayback() {
        DB.CRUD.getPlayback(Player.state.file.value!!.id).let { playback ->
            mediaPlayer.rate = playback.speed
            mediaPlayer.position = playback.progress
        }
    }

    override fun onDestroy() {
        stop()
        mediaPlayer.release()
        libVLC.release()
        DB.close()
        PreferenceUtil.close(applicationContext)
    }
}