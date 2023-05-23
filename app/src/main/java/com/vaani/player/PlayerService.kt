package com.vaani.player

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder
import android.util.Log
import android.view.SurfaceHolder
import com.vaani.models.FileEntity
import com.vaani.util.PreferenceUtil
import com.vaani.util.TAG
import java.io.FileDescriptor
import java.io.FileInputStream


class PlayerService : Service() {

    // Player context
    private lateinit var mediaPlayer: MediaPlayer

    override fun onCreate() {
        super.onCreate()
        mediaPlayer = MediaPlayer()
        mediaPlayer.setOnCompletionListener{
            Player.endReached()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Player.setPlayerService(this)
        Log.d(TAG, "onStartCommand: setting service")
        return START_STICKY
    }

    fun startMedia(file: FileEntity) {
        mediaPlayer.stop()
        mediaPlayer.setDataSource(getFileDescriptor(file))
        mediaPlayer.prepare()
        seekTo((file.playBackProgress*duration).toInt())
        speed = file.playBackSpeed
    }

    private fun getFileDescriptor(file: FileEntity): FileDescriptor {
        return if (file.isUri) {
            val parcelFileDescriptor =
                contentResolver.openFileDescriptor(Uri.parse(file.path), "r") ?: throw java.lang.RuntimeException()
            val fd = parcelFileDescriptor.fileDescriptor
            parcelFileDescriptor.detachFd()
            parcelFileDescriptor.close()
            fd
        } else {
            FileInputStream(java.io.File(file.path)).fd
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    var speed: Float
        get() = mediaPlayer.playbackParams.speed
        set(value) {
            mediaPlayer.playbackParams.speed = value

        }

    fun play() {
        mediaPlayer.start()
    }

    fun pause() {
        mediaPlayer.pause()
    }

    val duration: Int
        get() = mediaPlayer.duration

    val currentPosition: Int
        get() = mediaPlayer.currentPosition

    fun seekTo(position: Int) {
        mediaPlayer.seekTo(position)
    }

    val isPlaying: Boolean
        get() = mediaPlayer.isPlaying

    fun stop() {
        mediaPlayer.stop()
    }

    fun detachViews() {
        mediaPlayer.setDisplay(null)
    }

    override fun onDestroy() {
        stop()
        mediaPlayer.release()
    }

    fun attachView(surfaceHolder: SurfaceHolder) {
        mediaPlayer.setDisplay(surfaceHolder)
    }
}