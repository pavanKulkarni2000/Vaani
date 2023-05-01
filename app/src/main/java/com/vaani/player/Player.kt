package com.vaani.player

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vaani.db.DB
import com.vaani.models.File
import com.vaani.util.PlayBackUtil
import com.vaani.util.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.videolan.libvlc.util.VLCVideoLayout
import kotlin.random.Random

object Player {
    private var scope = CoroutineScope(Job())
    private lateinit var mediaPlayerService: PlayerService
    val state = PlayerState()

    val currentPosition
    get() = mediaPlayerService.currentPosition

    val duration
    get() = mediaPlayerService.duration

    fun init(activity: Activity) {
        val intent = Intent(activity.applicationContext, PlayerService::class.java)
        activity.startService(intent)
    }

    fun setPlayerService(medPlayerService: PlayerService){
        mediaPlayerService =medPlayerService
    }

    fun startNewMedia(file: File) {
        savePreference()
        state.setCurrentFile(file)
    }

    fun attachPlayerView(vlcVideoLayout: VLCVideoLayout) {
        mediaPlayerService.attachVlcVideoView(vlcVideoLayout)
        state.setAttached(true)
    }

    fun detachPlayerView(){
        mediaPlayerService.detachViews()
        state.setAttached(false)
    }

    fun stop() {
        savePreference()
        mediaPlayerService.stop()
        detachPlayerView()
        state.setCurrentFile(File())
    }

    fun seekTo(pos:Int){
        mediaPlayerService.seekTo(pos)
    }

    fun playPrevious() {
        val file = state.file.value!!
        val files = DB.CRUD.getFolderMediaList(file.folderId)
        val prevFile:File = if(state.shuffle.value==true){
            files[files.indices.random(PlayBackUtil.random)]
        }else {
            val currIndex = files.indexOf(file)
            if (currIndex == 0) {
                return
            }
            files[currIndex - 1]
        }
        startNewMedia(prevFile)
    }


    fun playNext() {
        val file = state.file.value!!
        val files = DB.CRUD.getFolderMediaList(file.folderId)
        val nextFile:File = if(state.shuffle.value==true){
            files[files.indices.random(PlayBackUtil.random)]
        }else {
            val currIndex = files.indexOf(file)
            if (currIndex == files.size - 1) {
                stop()
                return
            }
            files[currIndex + 1]
        }
        startNewMedia(nextFile)
    }

    private fun savePreference() {
        val file = state.file.value!!
        if (file.id > 0) {
            val progress = mediaPlayerService.currentPosition.toFloat() / mediaPlayerService.duration
            scope.launch {
                DB.CRUD.run {
                    val playBack = getPlayback(file.id)
                    playBack.progress = progress
                    upsertPlayback(playBack)
                    val pref = getCollectionPreference(file.folderId)
                    pref.lastPlayedId = file.id
                    upsertCollectionPreference(pref)
                }
            }
        }
    }

    fun endReached() {
        val file = state.file.value!!
        if (file.id > 0) {
            mediaPlayerService.seekTo(0)
            if(state.loop.value!=true) {
                playNext()
            } else {
                state.updatePlaying(true)
            }
        }
    }
    class PlayerState {

        private val scope = CoroutineScope(Job())

        private val _isPlaying = MutableLiveData(false)
        val isPlaying: LiveData<Boolean> = _isPlaying

        private val _speed = MutableLiveData(1F)
        val speed: LiveData<Float> = _speed

        private val _loop = MutableLiveData(false)
        val loop: LiveData<Boolean> = _loop

        private val _shuffle = MutableLiveData(false)
        val shuffle: LiveData<Boolean> = _shuffle

        private val _file = MutableLiveData(File())
        val file: LiveData<File> = _file


        private val _isAttached = MutableLiveData(false)
        val isAttached: LiveData<Boolean> = _isAttached

        fun setCurrentFile(file: File) {
            DB.CRUD.getPlayback(file.id).let { playBack ->
                _speed.value = playBack.speed
                _loop.value = playBack.loop
            }
            DB.CRUD.getCollectionPreference(file.folderId).let { collectionPreference ->
                _shuffle.value = collectionPreference.shuffle
            }
            _file.value = file
        }

        fun updateSpeed(speed: Float) {
            _speed.value = speed
            scope.launch {
                DB.CRUD.run {
                    val playBack = getPlayback(file.value!!.id)
                    playBack.speed = speed
                    upsertPlayback(playBack)
                }
            }
        }

        fun setAttached(attached: Boolean) {
            _isAttached.value = attached
        }

        fun updateLoop(loop:Boolean){
            _loop.value = loop
            scope.launch {
                DB.CRUD.run {
                    val playBack = getPlayback(file.value!!.id)
                    playBack.loop = loop
                    upsertPlayback(playBack)
                }
            }
        }

        fun updateShuffle(shuffle:Boolean){
            _shuffle.value = shuffle
            scope.launch {
                DB.CRUD.run {
                    val collection = getCollectionPreference(file.value!!.folderId)
                    collection.shuffle = shuffle
                    upsertCollectionPreference(collection)
                }
            }
        }

        fun updatePlaying(isPlaying:Boolean){
            if(isPlaying){
                mediaPlayerService.play()
            }else{
                mediaPlayerService.pause()
            }
            _isPlaying.value = isPlaying

        }
    }

}