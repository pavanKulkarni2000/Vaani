package com.vaani.ui.player

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import com.vaani.db.DB
import com.vaani.models.CollectionPreference
import com.vaani.models.File
import com.vaani.models.PlayBack
import com.vaani.util.PreferenceUtil
import com.vaani.util.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.videolan.libvlc.LibVLC
import org.videolan.libvlc.Media
import org.videolan.libvlc.MediaPlayer
import org.videolan.libvlc.util.VLCVideoLayout


class PlayerService : Service(), PlayerServiceListener {

    // Player context
    private lateinit var libVLC: LibVLC
    private lateinit var mediaPlayer: MediaPlayer
    private var file: File? = null

    private var playerViewListener :PlayerViewListener? = null
    private var scope = CoroutineScope(Job())

    override fun onCreate() {
        super.onCreate()
        libVLC = LibVLC(applicationContext)
        mediaPlayer = MediaPlayer(libVLC)
        mediaPlayer.setEventListener(eventHandler)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Player.mediaPlayerService = this
        return START_STICKY
    }

    private val eventHandler = MediaPlayer.EventListener { event ->
        when (event.type) {
            MediaPlayer.Event.EndReached -> {
                mediaPlayer.position = 0f
                playNext()
            }
            MediaPlayer.Event.SeekableChanged->{
                recallCurrentPlayback()
            }
        }
    }

    private fun startMedia(file:File){
        if(this.file!=file){
            stopCurrentMedia()
            val media = createMedia(file)
            mediaPlayer.media = media
            media.release()
            this.file = file
        }
        mediaPlayer.play()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override var speed: Float
        get() = mediaPlayer.rate
        set(value) {
            mediaPlayer.rate = value

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

    override fun startNewMedia(file: File) {
        startMedia(file)
    }

    private fun persistCollectionPreference() {
        this.file?.let{
                DB.CRUD.apply {
                    val pref = getCollectionPreference(it.folderId)?:CollectionPreference().apply { collectionId = it.folderId }
                    pref.lastPlayedId = it.id
                    upsertCollectionPreference(pref)
                }
        }
    }

    private fun stopCurrentMedia(){
        persistCurrentPlayback()
        mediaPlayer.stop()

    }

    override fun stop() {
        stopCurrentMedia()
        persistCollectionPreference()
        file = null
    }

    override fun attachVlcVideoView(vlcVideoLayout: VLCVideoLayout, videoListener: PlayerViewListener) {
        mediaPlayer.attachViews(vlcVideoLayout, null, false, false)
        this.playerViewListener = videoListener
    }

    override fun playNext() {
        file!!.let {
            val files = DB.CRUD.getFolderMediaList(it.folderId)
            val currIndex = files.indexOf(it)
            if (currIndex == files.size - 1) {
                stop()
                playerViewListener?.exit()
                return
            }
            val nextFile = files[currIndex + 1]
            startMedia(nextFile)
            playerViewListener?.mediaChanged(nextFile)
            file = nextFile
        }
    }


override fun playPrevious() {
        file!!.let {
            val files = DB.CRUD.getFolderMediaList(it.folderId)
            val currIndex = files.indexOf(it)
            if (currIndex == 0) {
                return
            }
            val prevFile = files[currIndex - 1]
            startMedia(prevFile)
            playerViewListener?.mediaChanged(prevFile)
            file = prevFile
        }
    }

    override fun detachViews() {
        mediaPlayer.detachViews()
        playerViewListener = null
    }

    override val currentMediaFile get() = file

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

    override fun recallCurrentPlayback() {
        file?.let{
            DB.CRUD.getPlayback(it.id)?.let {
                playback->
                mediaPlayer.rate = playback.speed
                mediaPlayer.position = playback.progress
            }
        }
    }

    override fun persistCurrentPlayback() {
        file?.let{
            DB.CRUD.run {
                val playBack = getPlayback(it.id)?: PlayBack().apply { fileId = it.id }
                playBack.apply {
                    progress = mediaPlayer.position
                    speed = mediaPlayer.rate
                }
                Log.d(TAG, "persistCurrentPlayback: $playBack")
                scope.launch {
                    upsertPlayback(playBack)
                }
            }
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