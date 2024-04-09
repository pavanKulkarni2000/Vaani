package com.vaani.db

import android.content.Context
import android.util.Log
import com.vaani.db.entity.FavouriteEntity
import com.vaani.db.entity.FavouriteEntity_
import com.vaani.db.entity.FileEntity
import com.vaani.db.entity.FolderEntity
import com.vaani.db.entity.FolderEntity_
import com.vaani.db.entity.MediaEntity
import com.vaani.db.entity.MediaEntity_
import com.vaani.db.entity.MyObjectBox
import com.vaani.model.Folder
import com.vaani.model.Media
import com.vaani.util.TAG
import io.objectbox.Box
import io.objectbox.BoxStore

object DB {

  private lateinit var store: BoxStore
  private lateinit var folderBox: Box<FolderEntity>
  private lateinit var fileBox: Box<MediaEntity>
  private lateinit var favouriteBox: Box<FavouriteEntity>

  fun init(context: Context) {
    store = MyObjectBox.builder().androidContext(context).build()
    folderBox = store.boxFor(FolderEntity::class.java)
    fileBox = store.boxFor(MediaEntity::class.java)
    favouriteBox = store.boxFor(FavouriteEntity::class.java)
  }

  fun getFolders(): List<FolderEntity> {
    return folderBox.all
  }

  fun getFolderFiles(folderId: Long): List<MediaEntity> {
    return folderBox[folderId].medias.toList()
  }

  fun getFolderWithPath(path: String): FolderEntity? {
    folderBox.query(FolderEntity_.path.equal(path)).build().use {
      return it.findFirst()
    }
  }

  fun isFavourite(mediaId: Long): MediaEntity? {
    favouriteBox.query(FavouriteEntity_.mediaId.equal(mediaId)).build().use {
      return it.findFirst()?.media?.target
    }
  }

  fun getFavourites(): List<FavouriteEntity> = favouriteBox.all

  fun getFavouriteCount() = favouriteBox.count()

  fun getFile(fileId: Long): MediaEntity = fileBox[fileId]

  fun getFiles(fileIds: List<Long>): List<MediaEntity> = fileBox[fileIds]

  fun save(favourite: FavouriteEntity) {
    favouriteBox.put(favourite)
  }

  fun saveFavourites(favourites: List<FavouriteEntity>) {
    favouriteBox.put(favourites)
  }

  fun saveMedia(files: MediaEntity) {
    fileBox.put(files)
  }

  fun saveMedias(files: List<MediaEntity>) {
    fileBox.put(files)
  }

  fun save(folder: FolderEntity) {
    folderBox.put(folder)
  }

  fun updateFolderFiles(folder: Folder, exploredFiles: List<Media>) {
    store.runInTx {
      val folderEntity =
        folderBox.query(FolderEntity_.path.equal(folder.path)).build().use { it.findFirst()!! }
      val exploredMediaPaths = exploredFiles.map(Media::path).toSet()
      folderEntity.medias.forEach {
        if (!exploredMediaPaths.contains(it.path)) {
          folderEntity.medias.remove(it)
          fileBox.remove(it)
        }
      }
      val dbMedias = folderEntity.medias.map(MediaEntity::path).toSet()
      exploredFiles.forEach {
        if (!dbMedias.contains(it.path)) {
          folderEntity.medias.add(
            MediaEntity(
              id = 0,
              name = it.name,
              path = it.path,
              isUri = it.isUri,
              isAudio = it.isAudio,
              duration = it.duration,
              playBackProgress = it.playBackProgress,
            )
          )
        }
      }
      folderEntity.medias.applyChangesToDb()
    }
  }

  fun updateFolders(exploredFolders: Set<Folder>) {
    val dbFolders = folderBox.all
    val deadFolders = mutableSetOf<FolderEntity>()
    val newFolders = exploredFolders.toMutableSet()
    dbFolders.forEach { file ->
      newFolders
        .find { it.path == file.path }
        ?.let {
          // already in DB
          exploredFile ->
          newFolders.remove(exploredFile)
        } ?: deadFolders.add(file)
    }
    val newDbFolders =
      newFolders.map { FolderEntity(id = 0, name = it.name, path = it.path, isUri = it.isUri) }
    dbFolders.addAll(newDbFolders)
    val folIds = dbFolders.map(FolderEntity::id).toLongArray()
    val deadMedias = fileBox.query(MediaEntity_.folderId.notOneOf(folIds)).build().use { it.find() }
    Log.d(
      TAG,
      "updateFolders: dead files ${deadMedias.size} ${deadMedias.map { FileEntity::name }}",
    )
    store.runInTx {
      folderBox.remove(deadFolders)
      folderBox.put(newDbFolders)
      fileBox.remove(deadMedias)
    }
  }

  fun deleteMedia(file: MediaEntity) {
    fileBox.remove(file)
  }

  fun deleteMedias(files: List<MediaEntity>) {
    fileBox.remove(files)
  }

  fun deleteFolder(folder: FolderEntity) {
    folderBox.remove(folder)
  }

  fun deleteFavourite(favourite: Long) {
    favouriteBox.remove(favourite)
  }

  fun close() {
    store.close()
  }

  fun resume(context: Context) {
    if (store.isClosed) {
      init(context)
    }
  }

  fun getFolder(id: Long): FolderEntity = folderBox[id]
}
