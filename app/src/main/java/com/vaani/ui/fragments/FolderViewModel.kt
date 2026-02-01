package com.vaani.ui.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vaani.dal.FileManager
import com.vaani.model.Folder
import kotlinx.coroutines.launch

class FolderViewModel(private val fileManager: FileManager) : ViewModel() {

    private val _folders = MutableLiveData<List<Folder>>()
    val folders: LiveData<List<Folder>> = _folders

    fun loadFolders() {
        viewModelScope.launch {
            _folders.value = fileManager.getFolders()
        }
    }

    fun refreshFolders() {
        viewModelScope.launch {
            fileManager.exploreFolders()
            // After exploring, reload the folders to reflect changes
            _folders.value = fileManager.getFolders()
        }
    }
}

class FolderViewModelFactory(private val fileManager: FileManager) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FolderViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FolderViewModel(fileManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
