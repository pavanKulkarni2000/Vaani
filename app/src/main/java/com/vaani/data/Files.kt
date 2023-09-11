package com.vaani.data

import android.net.Uri
import com.vaani.MainActivity
import com.vaani.data.util.FileUtil
import com.vaani.db.DB
import com.vaani.models.FavouriteEntity
import com.vaani.models.FolderEntity
import com.vaani.models.MediaEntity
import com.vaani.util.Constants.FAVOURITE_COLLECTION_ID
import com.vaani.util.PreferenceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.nio.file.Paths

object Files {


    val favouriteFolder = FolderEntity().apply { id = FAVOURITE_COLLECTION_ID }
    val allFolders: MutableList<FolderEntity> = mutableListOf()
    val favourites = mutableListOf<FavouriteEntity>()

    fun getFolder(id: Long): FolderEntity {
        return when (id) {
            FAVOURITE_COLLECTION_ID -> favouriteFolder
            else -> allFolders.find { folder -> folder.id == id } ?: FolderEntity()
        }
    }

    fun getFile(fileId: Long) = DB.getFile(fileId)

    fun getCollectionFiles(id: Long): List<MediaEntity> {
        return when (id) {
            FAVOURITE_COLLECTION_ID -> DB.getFiles(favourites.map(FavouriteEntity::fileId))
            else -> DB.getFolderFiles(id)
        }
    }

    fun updateLastPlayedItem(folderId: Long, lastPlayedId: Long) {
        when (folderId) {
            FAVOURITE_COLLECTION_ID -> favouriteFolder.lastPlayedId = lastPlayedId
            else -> {
                allFolders.find { folder -> folderId == folder.id }?.let {
                    it.lastPlayedId = lastPlayedId
                    DB.save(it)
                }
            }
        }
        PreferenceUtil.lastPlayedFolderId = folderId
        PreferenceUtil.save()
    }

    fun update(updatedFile: MediaEntity) {
        DB.save(listOf(updatedFile))
    }

    fun addFavourite(mediaEntity: MediaEntity): FavouriteEntity {
        if (favourites.map(FavouriteEntity::fileId).contains(mediaEntity.id)) {
            throw Exception("File already favorite")
        }
        val newFav = FavouriteEntity(mediaEntity, rank = favourites.size)
        DB.save(newFav)
        favourites.add(newFav)
        return newFav
    }

    fun remove(favEntity: FavouriteEntity) {
        DB.delete(favEntity)
        val favRank = favEntity.rank
        favourites.removeAt(favRank)
        val favSize = favourites.size
        for (i in favRank until favSize) {
            favourites[i].rank = i
            DB.save(favourites[i])
        }
    }

    fun moveFavourite(rankFrom: Int, rankTo: Int) {
        val tmp = favourites[rankFrom]
        if (rankTo > rankFrom) {
            for (i in rankFrom until rankTo) {
                favourites[i] = favourites[i + 1]
                favourites[i].rank = i
                DB.save(favourites[i])
            }
        } else {
            for (i in rankTo until rankFrom) {
                favourites[i + 1] = favourites[i]
                favourites[i + 1].rank = i + 1
                DB.save(favourites[i + 1])
            }
        }
        favourites[rankTo] = tmp
        favourites[rankTo].rank = rankTo
        DB.save(favourites[rankTo])
    }

    suspend fun exploreFolders() {
        coroutineScope {
            val differed1 = async { FileUtil.updatePrimaryStorageList() }
            val differed2 =
                async { FileUtil.updateSecondaryStorageList(MainActivity.context) }
            val differed3 = async { FileUtil.updateAndroidFolderList(MainActivity.context) }
            val folderMedias = (differed1.await() + differed2.await() + differed3.await())
            DB.updateFolders(folderMedias.keys)
            folderMedias.forEach { (folder, mediaList) ->
                allFolders.find(folder::equals)?.let { existingFolder ->
                    DB.updateFolderFiles(existingFolder, mediaList)
                } ?: run {
                    allFolders.add(folder)
                    DB.updateFolderFiles(folder, mediaList)
                }
            }
        }
    }

    suspend fun exploreFolder(folder: FolderEntity): List<MediaEntity> {
        val mediaList = withContext(Dispatchers.IO) {
            FileUtil.getMediaInFolder(
                MainActivity.context,
                folder
            )
        }
        DB.updateFolderFiles(folder, mediaList)
        return mediaList
    }

    fun init() {
        allFolders.clear()
        allFolders.addAll(DB.getFolders())
        favourites.clear()
        favourites.addAll(DB.getFavourites().sortedBy(FavouriteEntity::rank))
    }

    fun moveFile(sourceFile: MediaEntity, destinationUri: Uri): FolderEntity {
        FileUtil.moveFile(sourceFile, destinationUri)
        val folderPath = Paths.get(sourceFile.path).parent
        val folder: FolderEntity =
            allFolders.find { folder -> folder.path == folderPath.toString() }?.also {
                it.items++
            } ?: FolderEntity().also {
                it.name = folderPath.fileName.toString()
                it.path = folderPath.toString()
                it.isUri = false
                it.items = 1
                allFolders.add(it)
            }
        DB.save(folder)
        sourceFile.folderId = folder.id
        DB.save(listOf(sourceFile))
        return folder
    }

    fun copyFile(sourceFile: MediaEntity, destinationUri: Uri): FolderEntity {
        val newFile = FileUtil.copyFile(sourceFile, destinationUri)
        val folderPath = Paths.get(newFile.path).parent
        val folder: FolderEntity =
            allFolders.find { folder -> folder.path == folderPath.toString() }?.also {
                it.items++
            } ?: FolderEntity().also {
                it.name = folderPath.fileName.toString()
                it.path = folderPath.toString()
                it.isUri = false
                it.items = 1
                allFolders.add(it)
            }
        DB.save(folder)
        newFile.folderId = folder.id
        DB.save(listOf(newFile))
        return folder
    }

    fun rename(mediaEntity: MediaEntity, newName: String) {
        FileUtil.rename(mediaEntity, newName)
        DB.save(listOf(mediaEntity))
    }

    fun delete(file: MediaEntity) {
        FileUtil.delete(file)
        DB.delete(listOf(file))
        allFolders.find { folder -> folder.id == file.folderId }?.let { folder ->
            folder.items--
            if (folder.items == 0) {
                DB.delete(folder)
                allFolders.remove(folder)
            } else {
                DB.save(folder)
            }
        }
        favourites.find { fav -> fav.fileId == file.id }?.let {
            DB.delete(it)
            favourites.remove(it)
        }
    }

    fun rename(folder: FolderEntity, newName: String) {
        FileUtil.rename(folder, newName)
        DB.save(folder)
    }

    fun delete(folder: FolderEntity) {
        FileUtil.delete(folder)
        val medias = DB.getFolderFiles(folder.id)
        DB.delete(medias)
        val mediaIds = medias.map(MediaEntity::id).toSet()
        favourites.forEach {
            if (mediaIds.contains(it.fileId)) {
                remove(it)
            }
            favourites.remove(it)
        }
        DB.delete(folder)
        allFolders.remove(folder)
    }

}