package com.vaani.data

import android.content.Context
import android.net.Uri
import android.util.Log
import com.vaani.MainActivity
import com.vaani.data.util.FileUtil
import com.vaani.db.DB
import com.vaani.models.FavSortOrder
import com.vaani.models.FavouriteEntity
import com.vaani.models.FileEntity
import com.vaani.models.FolderEntity
import com.vaani.util.Constants.FAVOURITE_COLLECTION_ID
import com.vaani.util.PreferenceUtil
import com.vaani.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

object Files {

    lateinit var favouriteRanks: Map<Long, Int>
        private set
    val allFolders: MutableList<FolderEntity> = mutableListOf()
    var currentFolder: FolderEntity = PreferenceUtil.favouriteFolder
        private set
    val currentFiles: MutableList<FileEntity> = mutableListOf()
    val favourites: MutableList<FileEntity> = mutableListOf()
    var favSortOrder = FavSortOrder.RANK

    fun setCurrentFolder(folderEntity: FolderEntity) {
        currentFolder = folderEntity
        resetFolder(folderEntity)
    }

    fun getFolder(id: Long): FolderEntity {
        return when (id) {
            FAVOURITE_COLLECTION_ID -> PreferenceUtil.favouriteFolder
            else -> allFolders.find { folder -> folder.id == id } ?: FolderEntity()
        }
    }

    fun getFile(fileId: Long) = DB.getFile(fileId)

    fun getFolderFiles(id: Long): MutableList<FileEntity> {
        return when (id) {
            FAVOURITE_COLLECTION_ID -> favourites
            currentFolder.id -> currentFiles
            else -> DB.getFolderFiles(id).toMutableList()
        }
    }

    fun updateLastPlayedItem(folderId: Long, lastPlayedId: Long) {
        when (folderId) {
            FAVOURITE_COLLECTION_ID -> PreferenceUtil.favouriteFolder.lastPlayedId = lastPlayedId
            else -> {
                allFolders.find { folder -> folderId == folder.id }?.let {
                    it.lastPlayedId = lastPlayedId
                    DB.save(it)
                }
            }
        }
    }

    fun update(updatedFile: FileEntity) {
        Log.d(TAG, "saveProgress: saving $updatedFile")
        val isFavourite = false
        if (updatedFile.folderId == FAVOURITE_COLLECTION_ID) {
            updatedFile.folderId = DB.getFile(updatedFile.id).folderId
        }
        DB.save(updatedFile)
        if (isFavourite) {
            updatedFile.folderId = FAVOURITE_COLLECTION_ID
        }
    }

    fun addFavourite(fileEntity: FileEntity) {
        if (favourites.contains(fileEntity)) {
            throw Exception("Favourite already present")
        }
        favourites.add(fileEntity)
        DB.insertFavourite(FavouriteEntity(id = 0, fileId = fileEntity.id, rank = favourites.size - 1))
    }

    fun removeFavourite(position: Int) {
        val file = favourites.removeAt(position)
        removeFavouriteFromDB(file.id)
    }

    private fun removeFavouriteFromDB(fileId: Long) {
        val favEntities = DB.getFavourites().toMutableList()
        val favEntity = favEntities.find { it.fileId == fileId }!!
        DB.deleteFavourite(favEntity)
        favEntities.remove(favEntity)
        for (i in favEntity.rank until favEntities.size) {
            favEntities[i].rank = i
        }
        DB.updateFavourites(favEntities)
    }

    fun moveFavourite(rankFrom: Int, rankTo: Int) {
        val favEntities = DB.getFavourites()
        val favUpdates = buildList {
            val tmp = favourites[rankFrom]
            if (rankTo > rankFrom) {
                for (i in rankFrom until rankTo) {
                    favourites[i] = favourites[i + 1]
                    add(favEntities.find { it.fileId == favourites[i].id }!!.apply { this.rank = i })
                }
            } else {
                for (i in rankTo until rankFrom) {
                    favourites[i + 1] = favourites[i]
                    add(favEntities.find { it.fileId == favourites[i + 1].id }!!.apply { this.rank = i + 1 })
                }
            }
            favourites[rankTo] = tmp
            add(favEntities.find { it.fileId == favourites[rankTo].id }!!.apply { this.rank = rankTo })
        }
        DB.updateFavourites(favUpdates)
    }

    suspend fun explore() {
        coroutineScope {
            val differed1 = async { FileUtil.updatePrimaryStorageList() }
            val differed2 =
                async { FileUtil.updateSecondaryStorageList(MainActivity.context) }
            val differed3 = async { FileUtil.updateAndroidFolderList(MainActivity.context) }
            val folderMedias = (differed1.await() + differed2.await() + differed3.await())
            DB.updateFolders(folderMedias.keys)
            resetAllFolders()
            folderMedias.forEach { (folder, mediaList) ->
                allFolders.first(folder::equals).let { dbFolder ->
                    DB.updateFolderFiles(dbFolder, mediaList)
                }
            }
        }
    }

    suspend fun exploreFolder(folder: FolderEntity) {
        coroutineScope {
            val mediaList = withContext(Dispatchers.IO) {
                FileUtil.getMediaInFolder(
                    MainActivity.context,
                    folder
                )
            }
            DB.updateFolderFiles(folder, mediaList)
        }
    }

    fun init() {
        resetAllFolders()
        favouriteRanks = DB.getFavourites().associate { it.fileId to it.rank }
        resetFolder(PreferenceUtil.favouriteFolder)
    }

    private fun resetAllFolders() {
        allFolders.apply {
            clear()
            addAll(DB.getFolders())
        }
    }

    private fun resetFolder(folderEntity: FolderEntity): MutableList<FileEntity> {
        return when (folderEntity.id) {
            FAVOURITE_COLLECTION_ID -> favourites.apply {
                clear()
                addAll(DB.getFavouriteFiles())
                when (favSortOrder) {
                    FavSortOrder.ASC, FavSortOrder.DSC -> sortBy(FileEntity::name)
                    FavSortOrder.RANK -> sortBy { favouriteRanks[it.id] }
                }
            }
            else -> currentFiles.apply {
                clear()
                addAll(DB.getFolderFiles(folderEntity.id))
            }
        }

    }

    fun search(folder: FolderEntity, query: String?) {
        val files = resetFolder(folder)
        if (query != null && query.isNotBlank()) {
            files.retainAll { it.name.contains(query) }
        }
    }

    fun updateFavouriteSort(sortOrder: FavSortOrder) {
        favSortOrder = sortOrder
        when (favSortOrder) {
            FavSortOrder.ASC, FavSortOrder.DSC -> favourites.sortBy(FileEntity::name)
            FavSortOrder.RANK -> favourites.sortBy { favouriteRanks[it.id] }
        }
    }

    suspend fun copyFile(sourceFile: FileEntity, destinationUri: Uri?, context: Context) {

        FileUtil.getPath(context, destinationUri)?.let { path ->
            Log.d(TAG, "copyFile: path = $path")
            FileUtil.copyFile(sourceFile, path.resolve(sourceFile.name))
            val folder: FolderEntity = allFolders.find { fol -> fol.path == path.toString() } ?: FolderEntity().also {
                it.path = path.toString(); it.name = path.fileName.toString(); DB.save(it); allFolders.add(it); allFolders.sortBy { fol->fol.name.lowercase() }
            }
            exploreFolder(folder)
        }
    }

}