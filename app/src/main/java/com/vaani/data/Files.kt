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
  val folders: List<FolderEntity>
    get() = DB.getFolders()

  val favourites: List<FavouriteEntity>
    get() = DB.getFavourites()

  val favouriteFolder = FolderEntity().apply { id = FAVOURITE_COLLECTION_ID }

  fun getFolderMedias(id: Long): List<MediaEntity> {
    return DB.getFolderFiles(id)
  }

  fun updateLastPlayedItem(folderId: Long, lastPlayedId: Long) {
    when (folderId) {
      FAVOURITE_COLLECTION_ID -> favouriteFolder.lastPlayedId = lastPlayedId
      else -> {
        DB.getFolder(folderId).let {
          it.lastPlayedId = lastPlayedId
          DB.save(it)
        }
      }
    }
    PreferenceUtil.lastPlayedFolderId = folderId
    PreferenceUtil.save()
  }

  fun update(updatedFile: MediaEntity) {
    DB.saveMedia(updatedFile)
  }

  fun addFavourite(mediaEntity: MediaEntity): FavouriteEntity {
    val favourites = DB.getFavourites()
    if (favourites.map(FavouriteEntity::fileId).contains(mediaEntity.id)) {
      throw Exception("File already favorite")
    }
    val newFav = FavouriteEntity(mediaEntity, rank = favourites.size)
    DB.save(newFav)
    return newFav
  }

  fun remove(favEntity: FavouriteEntity) {
    DB.deleteFavourite(favEntity)
    val deletedRank = favEntity.rank
    val favourites = DB.getFavourites()
    for (fav in favourites) {
      if (fav.rank > deletedRank) {
        fav.rank--
      }
    }
    DB.saveFavourites(favourites)
  }

  fun moveFavourite(rankFrom: Int, rankTo: Int) {
    val favourites = DB.getFavourites().sortedBy(FavouriteEntity::rank)
    if (rankFrom < rankTo) {
      for (i in rankFrom until rankTo) {
        favourites[i + 1].rank = i
      }
    } else {
      for (i in rankTo until rankFrom) {
        favourites[i].rank = i + 1
      }
    }
    favourites[rankFrom].rank = rankTo
    DB.saveFavourites(favourites)
  }

  suspend fun exploreFolders() {
    coroutineScope {
      val differed1 = async { FileUtil.updatePrimaryStorageList() }
      val differed2 = async { FileUtil.updateSecondaryStorageList(MainActivity.context) }
      val differed3 = async { FileUtil.updateAndroidFolderList(MainActivity.context) }
      val folderMedias = (differed1.await() + differed2.await() + differed3.await())
      DB.updateFolders(folderMedias.keys)
      folderMedias.forEach { (folder, mediaList) -> DB.updateFolderFiles(folder, mediaList) }
    }
  }

  suspend fun exploreFolder(folder: FolderEntity): List<MediaEntity> {
    val mediaList =
      withContext(Dispatchers.IO) { FileUtil.getMediaInFolder(MainActivity.context, folder) }
    DB.updateFolderFiles(folder, mediaList)
    return mediaList
  }

  fun moveFile(sourceFile: MediaEntity, destinationUri: Uri): FolderEntity {
    FileUtil.moveFile(sourceFile, destinationUri)
    val folderPath = Paths.get(sourceFile.path).parent
    val folder: FolderEntity =
      DB.getFolderWithPath(folderPath.toString())?.also { it.items++ }
        ?: FolderEntity().also {
          it.name = folderPath.fileName.toString()
          it.path = folderPath.toString()
          it.isUri = false
          it.items = 1
        }
    DB.save(folder)
    sourceFile.folderId = folder.id
    DB.saveMedia(sourceFile)
    return folder
  }

  fun copyFile(sourceFile: MediaEntity, destinationUri: Uri): FolderEntity {
    val newFile = FileUtil.copyFile(sourceFile, destinationUri)
    val folderPath = Paths.get(newFile.path).parent
    val folder: FolderEntity =
      DB.getFolderWithPath(folderPath.toString())?.also { it.items++ }
        ?: FolderEntity().also {
          it.name = folderPath.fileName.toString()
          it.path = folderPath.toString()
          it.isUri = false
          it.items = 1
        }
    DB.save(folder)
    newFile.folderId = folder.id
    DB.saveMedia(newFile)
    return folder
  }

  fun renameFile(mediaEntity: MediaEntity, newName: String) {
    FileUtil.rename(mediaEntity, newName)
    DB.saveMedia(mediaEntity)
  }

  fun deleteMedia(file: MediaEntity) {
    FileUtil.delete(file)
    DB.deleteMedia(file)
    DB.getFolder(file.folderId).let { folder ->
      folder.items--
      if (folder.items == 0) {
        DB.deleteFolder(folder)
      } else {
        DB.save(folder)
      }
    }
    DB.getFavourites().find { fav -> fav.fileId == file.id }?.let { DB.deleteFavourite(it) }
  }

  fun renameFolder(folder: FolderEntity, newName: String) {
    FileUtil.rename(folder, newName)
    DB.save(folder)
  }

  fun deleteFolder(folder: FolderEntity) {
    FileUtil.delete(folder)
    val medias = DB.getFolderFiles(folder.id)
    DB.deleteMedias(medias)
    val mediaIds = medias.map(MediaEntity::id).toSet()
    DB.getFavourites().forEach {
      if (mediaIds.contains(it.fileId)) {
        DB.deleteFavourite(it)
      }
    }
    DB.deleteFolder(folder)
  }

  fun getFiles(fileIds: List<Long>): List<MediaEntity> {
    return DB.getFiles(fileIds)
  }
}
