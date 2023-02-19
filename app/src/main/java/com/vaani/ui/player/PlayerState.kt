package com.vaani.ui.player

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.vaani.db.DB
import com.vaani.models.CollectionPreference
import com.vaani.models.File
import com.vaani.models.PlayBack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PlayerState {

    private val scope = CoroutineScope(Job())

    private val _playback = MutableLiveData(PlayBack())
    val playBack: LiveData<PlayBack> = _playback

    private val _file = MutableLiveData(File())
    val file: LiveData<File> = _file

    private val _collectionPreference = MutableLiveData(CollectionPreference())
    val collectionPreference: LiveData<CollectionPreference> = _collectionPreference

    private val _isAttached = MutableLiveData(false)
    val isAttached: LiveData<Boolean> = _isAttached

    fun setCurrentFile(file:File){
        _file.value = file
        if(file.folderId!= collectionPreference.value!!.collectionId ) {
            _collectionPreference.value = DB.CRUD.getCollectionPreference(file.id)
        }
    }

    fun updateCollection(collectionPreference: CollectionPreference){
        _collectionPreference.value = collectionPreference
        scope.launch {
            DB.CRUD.upsertCollectionPreference(collectionPreference)
        }
    }

    fun updatePlayBack(playBack: PlayBack){
        _playback.value = playBack
        scope.launch{
            DB.CRUD.upsertPlayback(playBack)
        }
    }

    fun setAttached(attached:Boolean){
        _isAttached.value = attached
    }
}