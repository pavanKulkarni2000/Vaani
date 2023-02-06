package com.vaani.ui.player

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import com.vaani.db.DB
import com.vaani.models.File
import com.vaani.models.Folder
import com.vaani.util.TAG
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout
import java.lang.ref.WeakReference


class PlayerService : Service(), PlayerServiceListener {

    private lateinit var libVLC: LibVLC
    private lateinit var mediaPlayer: MediaPlayer
    private var file: File? = null
     var vlcPlayerFragment = WeakReference<VlcPlayerFragment>(null)

    override fun onCreate() {
        super.onCreate()
        libVLC = LibVLC(applicationContext)
        mediaPlayer = MediaPlayer(libVLC)
        mediaPlayer.setEventListener(eventHandler)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Player.mediaPlayerService = this
        Log.d(TAG, "onStartCommand: started")
        return START_STICKY
    }

    private val eventHandler = MediaPlayer.EventListener { event ->
        when (event.type) {
            MediaPlayer.Event.EndReached -> {
                file?.let {
                    DB.CRUD.upsertPlayBack(it.id,0f)
                }
                playNext()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun start() {
        mediaPlayer.play()
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override val duration: Int
        get() = mediaPlayer.length.toInt()

    override val currentPosition: Int
        get() = (mediaPlayer.position * duration).toInt()

    override fun seekTo(position: Int) {
        mediaPlayer.position = position.toFloat() / duration
    }

    override val isPlaying: Boolean
        get() = mediaPlayer.isPlaying

    override fun startMedia(file: File) {
        mediaPlayer.stop()
        if(this.file==null || this.file!=file) {
            val media = createMedia(file)
            mediaPlayer.media = media
            media.release()
        }
        this.file = file
        DB.CRUD.getPlayback(file.id)?.let {
            mediaPlayer.rate = it.speed
            mediaPlayer.position = it.progress
        }
        mediaPlayer.play()
    }

    override fun stop() {
        mediaPlayer.stop()
        file?.let{ DB.CRUD.upsertPlayBack(it.id, mediaPlayer.position) }
        file = null
    }

    override fun attachVlcVideoView(vlcVideoLayout: VLCVideoLayout) {
        mediaPlayer.attachViews(vlcVideoLayout, null, false, false)
    }

    override fun playNext() {
        file!!.let {
            val files = DB.CRUD.getFolderMediaList(Folder().apply { id = it.folderId })
            val currIndex = files.indexOf(it)
            if (currIndex == files.size - 1) {
                stop()
                vlcPlayerFragment.get()?.exit()
                return
            }
            val nextFile = files[currIndex + 1]
            startMedia(nextFile)
            vlcPlayerFragment.get()?.mediaChanged(nextFile)
            file = nextFile
        }
    }


override fun playPrevious() {
        file!!.let {
            val files = DB.CRUD.getFolderMediaList(Folder().apply { id = it.folderId })
            val currIndex = files.indexOf(it)
            if (currIndex == 0) {
                return
            }
            val prevFile = files[currIndex - 1]
            startMedia(prevFile)
            vlcPlayerFragment.get()?.mediaChanged(prevFile)
            file = prevFile
        }
    }

    override fun detachViews() {
        mediaPlayer.detachViews()
    }

    override val currentMediaFile get() = file

    override fun bind(vlcPlayerFragment: VlcPlayerFragment) {
        this.vlcPlayerFragment = WeakReference(vlcPlayerFragment)
    }

    override fun createMedia(file: File): Media {
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

    override fun getDuration(file: File): Long {
        libVLC
        val mp = MediaPlayer(libVLC)
        mp.media = createMedia(file)
        mp.play()
        mp.stop()
        return mp.length
    }

}