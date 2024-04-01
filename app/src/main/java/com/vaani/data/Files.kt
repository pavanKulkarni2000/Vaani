package com.vaani.data

import com.vaani.data.model.Favourite
import com.vaani.data.model.Folder
import com.vaani.data.model.Media
import com.vaani.ui.MainActivity
import com.vaani.data.util.FileUtil
import com.vaani.data.util.toFavourite
import com.vaani.data.util.toFolder
import com.vaani.data.util.toMedia
import com.vaani.db.DB
import com.vaani.db.entity.FavouriteEntity
import com.vaani.db.entity.FolderEntity
import com.vaani.db.entity.MediaEntity
import com.vaani.util.Constants.FAVOURITE_COLLECTION_ID
import com.vaani.util.PreferenceUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

object Files {
  val folders: List<Folder>
    get() = DB.getFolders().map(FolderEntity::toFolder)

  val favourites: List<Favourite>
    get() = DB.getFavourites().map(FavouriteEntity::toFavourite)


  fun getFolderMedias(id: Long): List<Media> {
    return DB.getFolderFiles(id).map(MediaEntity::toMedia)
  }

  fun updateLastPlayedItems(folderId: Long, lastPlayedId: Long) {
    when (folderId) {
      FAVOURITE_COLLECTION_ID -> PreferenceUtil.lastPlayedFavouriteId = lastPlayedId
      else -> {
        DB.getFolder(folderId).let {
          it.lastPlayedMedia?.targetId = lastPlayedId
          DB.save(it)
        }
      }
    }
    PreferenceUtil.lastPlayedFolderId = folderId
  }

  fun saveProgress(media: Media) {
    val dbMedia = DB.getFile(media.id)
    dbMedia.playBackProgress = media.playBackProgress
    DB.saveMedia(dbMedia)
  }

  fun addFavourite(media: Media): Favourite {
    val favourites = DB.isFavourite(media.id)
    if (favourites!=null) {
      throw Exception("File already favorite")
    }
    val newFav = FavouriteEntity(0,DB.getFavouriteCount().toInt())
    newFav.media.targetId=media.id
    DB.save(newFav)
    return newFav.toFavourite()
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

  suspend fun exploreFolder(folder: Folder) {
    val mediaList =
      withContext(Dispatchers.IO) { FileUtil.getMediaInFolder(MainActivity.context, folder) }
    DB.updateFolderFiles(folder, mediaList)
  }

//  fun moveFile(sourceFile: MediaEntity, destinationUri: Uri): FolderEntity {
//    FileUtil.moveFile(sourceFile, destinationUri)
//    val folderPath = Paths.get(sourceFile.path).parent
//    val folder: FolderEntity =
//      DB.getFolderWithPath(folderPath.toString())?.also { it.items++ }
//        ?: FolderEntity().also {
//          it.name = folderPath.fileName.toString()
//          it.path = folderPath.toString()
//          it.isUri = false
//          it.items = 1
//        }
//    DB.save(folder)
//    sourceFile.folderId = folder.id
//    DB.saveMedia(sourceFile)
//    return folder
//  }

//  fun copyFile(sourceFile: MediaEntity, destinationUri: Uri): FolderEntity {
//    val newFile = FileUtil.copyFile(sourceFile, destinationUri)
//    val folderPath = Paths.get(newFile.path).parent
//    val folder: FolderEntity =
//      DB.getFolderWithPath(folderPath.toString())?.also { it.items++ }
//        ?: FolderEntity().also {
//          it.name = folderPath.fileName.toString()
//          it.path = folderPath.toString()
//          it.isUri = false
//          it.items = 1
//        }
//    DB.save(folder)
//    newFile.folderId = folder.id
//    DB.saveMedia(newFile)
//    return folder
//  }

//  fun renameFile(mediaEntity: MediaEntity, newName: String) {
//    FileUtil.rename(mediaEntity, newName)
//    DB.saveMedia(mediaEntity)
//  }

//  fun deleteMedia(file: MediaEntity) {
//    FileUtil.delete(file)
//    DB.deleteMedia(file)
//    DB.getFolder(file.folderId).let { folder ->
//      folder.items--
//      if (folder.items == 0) {
//        DB.deleteFolder(folder)
//      } else {
//        DB.save(folder)
//      }
//    }
//    DB.getFavourites().find { fav -> fav.fileId == file.id }?.let { DB.deleteFavourite(it) }
//  }

//  fun renameFolder(folder: FolderEntity, newName: String) {
//    FileUtil.rename(folder, newName)
//    DB.save(folder)
//  }

//  fun deleteFolder(folder: FolderEntity) {
//    FileUtil.delete(folder)
//    val medias = DB.getFolderFiles(folder.id)
//    DB.deleteMedias(medias)
//    val mediaIds = medias.map(MediaEntity::id).toSet()
//    DB.getFavourites().forEach {
//      if (mediaIds.contains(it.fileId)) {
//        DB.deleteFavourite(it)
//      }
//    }
//    DB.deleteFolder(folder)
//  }

  fun getFiles(fileIds: List<Long>): List<MediaEntity> {
    return DB.getFiles(fileIds)
  }
}
