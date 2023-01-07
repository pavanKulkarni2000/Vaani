package com.vaani.ui.folderMediaList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vaani.db.DB
import com.vaani.models.File
import com.vaani.models.Folder
import com.vaani.util.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FolderMediaViewModel(application: Application, private val folder: Folder) : AndroidViewModel(application) {

    private val _folderMediaList = MutableLiveData(DB.CRUD.getFolderMediaList(folder))
    val folderMediaList: LiveData<List<File>> = _folderMediaList

    suspend fun updateFolderMedia() {
        val mediaList = FileUtil.getMediaInFolder(getApplication(), folder)

        viewModelScope.launch(Dispatchers.Main) {
            _folderMediaList.value = DB.CRUD.upsertFolderMediaList(folder, mediaList)
        }
    }

    class Factory(private val application: Application, private val folder: Folder) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(FolderMediaViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return FolderMediaViewModel(application, folder) as T
            }
            throw Exception("Unable to create view model")
        }
    }

}