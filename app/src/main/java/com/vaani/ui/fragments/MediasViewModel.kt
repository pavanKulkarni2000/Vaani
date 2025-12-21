package com.vaani.ui.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vaani.dal.FileManager
import com.vaani.dal.MediaManager
import com.vaani.model.Folder
import com.vaani.model.Media
import kotlinx.coroutines.launch

class MediasViewModel(private val mediaManager: MediaManager, private val fileManager: FileManager) : ViewModel() {

    private val _medias = MutableLiveData<List<Media>>()
    val medias: LiveData<List<Media>> = _medias

    fun loadMedias(folder: Folder) {
        viewModelScope.launch {
            _medias.value = mediaManager.getFolderMedias(folder.id)
        }
    }

    fun refreshMedias(folder: Folder) {
        viewModelScope.launch {
            fileManager.exploreFolder(folder)
            _medias.value = mediaManager.getFolderMedias(folder.id)
        }
    }
}

class MediasViewModelFactory(private val mediaManager: MediaManager, private val fileManager: FileManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MediasViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MediasViewModel(mediaManager, fileManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
