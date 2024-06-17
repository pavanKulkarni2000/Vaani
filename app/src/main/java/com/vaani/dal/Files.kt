package com.vaani.dal

import com.vaani.MainActivity
import com.vaani.db.DB
import com.vaani.db.entity.FolderEntity
import com.vaani.db.entity.MediaEntity
import com.vaani.model.Folder
import com.vaani.model.Media
import com.vaani.model.Search
import com.vaani.util.FileUtil
import com.vaani.util.toFolder
import com.vaani.util.toMedia
import com.vaani.util.toSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext

object Files {
  val folders: List<Folder>
    get() = DB.getFolders().map(FolderEntity::toFolder)

  fun searchFolders(query:String) : List<Search> = DB.searchFolders(query).map(FolderEntity::toSearch)

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

  fun getMedias(fileIds: List<Long>): List<Media> {
    return DB.getFiles(fileIds).map(MediaEntity::toMedia)
  }
}
