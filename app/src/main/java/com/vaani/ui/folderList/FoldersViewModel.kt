package com.vaani.ui.folderList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.vaani.db.DB
import com.vaani.util.Constants
import com.vaani.util.FileUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class FoldersViewModel(application: Application) : AndroidViewModel(application) {

    private var _folderList = MutableLiveData(DB.CRUD.getFolders())
    val folderList = _folderList

    suspend fun refreshAllData() {
        coroutineScope {
            val differed1 = async { FileUtil.updatePrimaryStorageList() }
            val differed2 =
                async { FileUtil.updateSecondaryStorageList(getApplication<Application>().applicationContext) }
            val differed3 = async { FileUtil.updateAndroidFolderList(getApplication<Application>().applicationContext) }
            val folderMedias = (differed1.await() + differed2.await() + differed3.await())
            val dbFolders = DB.CRUD.upsertFolders(folderMedias.keys)
            viewModelScope.launch(Dispatchers.Main) {
                _folderList.value = dbFolders
            }
            launch(Dispatchers.IO) {
                folderMedias.forEach { (folder, mediaList) ->
                    dbFolders.first(folder::equals).let { dbFolder ->
                        mediaList.forEach { file -> file.folderId = dbFolder.id }
                        DB.CRUD.upsertFolderMediaList(dbFolder, mediaList)
                    }
                }
                DB.CRUD.updatePlaybacks(folderMedias.values.flatten().toSet())
            }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            if (modelClass.isAssignableFrom(FoldersViewModel::class.java)) {
                return FoldersViewModel(application) as T
            }
            throw IllegalArgumentException(Constants.VIEWMODEL_FACTORY_ERROR)
        }
    }
}