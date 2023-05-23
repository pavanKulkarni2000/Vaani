package com.vaani.data

import androidx.lifecycle.MutableLiveData
import com.vaani.MainActivity
import com.vaani.data.util.FileUtil
import com.vaani.db.DB
import com.vaani.models.FavouriteEntity
import com.vaani.models.FileEntity
import com.vaani.models.FolderEntity
import com.vaani.util.Constants
import com.vaani.util.PreferenceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

object Files {

    val allFoldersLive= MutableLiveData<List<FolderEntity>>(emptyList())
    val currentFolderLive= MutableLiveData(FolderEntity())
    val currentFilesLive= MutableLiveData<List<FileEntity>>(emptyList())
    val favouritesLive= MutableLiveData<List<FileEntity>>(emptyList())

    val allFolders: List<FolderEntity>
        get() = allFoldersLive.value!!
    val currentFolder: FolderEntity
        get() = currentFolderLive.value!!
    val currentFiles: List<FileEntity>
        get() = currentFilesLive.value!!
    val favourites: List<FileEntity>
        get() = favouritesLive.value!!

    fun changeCurrentFolder(folderEntity: FolderEntity) {
        currentFolderLive.value = folderEntity
        currentFilesLive.value = DB.getFolderFiles(folderEntity.id)
    }

    fun getFolder(id: Long): FolderEntity {
        return when (id) {
            Constants.FAVOURITE_COLLECTION_ID -> PreferenceUtil.favouriteFolder
            else -> allFolders.find { folder -> folder.id == id } ?: FolderEntity()
        }
    }

    fun getFolderFiles(id: Long): List<FileEntity> {
        return when (id) {
            Constants.FAVOURITE_COLLECTION_ID -> DB.getFavouriteFiles()
            else -> DB.getFolderFiles(id)
        }
    }

    fun updateFolder(updatedFolder: FolderEntity) {
        DB.save(updatedFolder)
        val index = allFolders.indexOfFirst { folder -> updatedFolder.id == folder.id }
        allFolders[index].copyPreferenceFrom(updatedFolder)
    }

    fun updateFile(updatedFile: FileEntity) {
        DB.save(updatedFile)
        if (currentFolder.id == updatedFile.folderId) {
            val index = currentFiles.indexOfFirst { file -> updatedFile.id == file.id }
            currentFiles[index].copyPreferenceFrom(updatedFile)
        }
    }

    fun addFavourite(fileEntity: FileEntity) {
        val newList = favourites.toMutableList().apply { add(fileEntity) }
        favouritesLive.value = newList
        DB.insertFavourite(FavouriteEntity(id = 0, fileId = fileEntity.id, rank = newList.size - 1))
    }

    fun removeFavourite(rank: Int) {
        removeFavouriteFromDB(favourites[rank].id)
        val newList = favourites.toMutableList().apply { removeAt(rank) }
        favouritesLive.value = newList
    }

    fun removeFavourite(fileEntity: FileEntity) {
        val newList = favourites.toMutableList().apply { remove(fileEntity) }
        favouritesLive.value = newList
        removeFavouriteFromDB(fileEntity.id)
    }

    private fun removeFavouriteFromDB(fileId:Long){
        val favEntities = DB.getFavourites().toMutableList()
        val favEntity = favEntities.find { it.fileId==fileId }!!
        DB.deleteFavourite(favEntity)
        favEntities.remove(favEntity)
        for (i in favEntity.rank until favEntities.size) {
            favEntities[i].rank = i
        }
        DB.updateFavourites(favEntities)
    }

    fun moveFavourite(rankFrom: Int, rankTo: Int) {
        val favEntities = DB.getFavourites()
        val newList = favourites.toMutableList()
        val favUpdates = buildList {
            val tmp = favourites[rankFrom]
            if (rankTo > rankFrom) {
                for (i in rankFrom until rankTo) {
                    newList[i] = newList[i + 1]
                    add(favEntities.find { it.id == newList[i].id }!!.apply { this.rank = i })
                }
            } else {
                for (i in rankTo until rankFrom) {
                    newList[i + 1] = newList[i]
                    add(favEntities.find { it.id == newList[i + 1].id }!!.apply { this.rank = i + 1 })
                }
            }
            newList[rankTo] = tmp
            add(favEntities.find { it.id == newList[rankTo].id }!!.apply { this.rank = rankTo })
        }
        favouritesLive.value = newList
        DB.updateFavourites(favUpdates)
    }

    suspend fun explore() {
        coroutineScope {
            val differed1 = async { FileUtil.updatePrimaryStorageList() }
            val differed2 =
                async { FileUtil.updateSecondaryStorageList(MainActivity.context) }
            val differed3 = async { FileUtil.updateAndroidFolderList(MainActivity.context) }
            val folderMedias = (differed1.await() + differed2.await() + differed3.await())
            val dbFolders = DB.updateFolders(folderMedias.keys)
            launch(Dispatchers.Main) {
                allFoldersLive.value = dbFolders
                val dbFolder = dbFolders.find(currentFolder::equals)
                currentFolderLive.value = dbFolder ?: FolderEntity()
                currentFilesLive.value = folderMedias[dbFolder]
            }
            folderMedias.forEach { (folder, mediaList) ->
                dbFolders.first(folder::equals).let { dbFolder ->
                    mediaList.forEach { file -> file.folderId = dbFolder.id }
                    DB.updateFolderFiles(dbFolder, mediaList)
                }
            }
        }
    }

    fun exploreFolder(folderEntity: FolderEntity) {
    }

    fun init(){
        allFoldersLive.value = DB.getFolders()
    }

}