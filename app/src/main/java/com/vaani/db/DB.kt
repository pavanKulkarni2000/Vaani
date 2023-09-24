package com.vaani.db

import android.content.Context
import android.util.Log
import com.vaani.models.FavouriteEntity
import com.vaani.models.FileEntity
import com.vaani.models.FolderEntity
import com.vaani.models.MediaEntity
import com.vaani.models.MediaEntity_
import com.vaani.models.MyObjectBox
import com.vaani.util.TAG
import io.objectbox.Box
import io.objectbox.BoxStore

object DB {

    private lateinit var store: BoxStore
    private lateinit var folderBox: Box<FolderEntity>
    private lateinit var fileBox: Box<MediaEntity>
    private lateinit var favouriteBox: Box<FavouriteEntity>

    fun init(context: Context) {
        store = MyObjectBox.builder()
            .androidContext(context)
            .build()
        folderBox = store.boxFor(FolderEntity::class.java)
        fileBox = store.boxFor(MediaEntity::class.java)
        favouriteBox = store.boxFor(FavouriteEntity::class.java)
    }

    fun getFolders(): List<FolderEntity> {
        return folderBox.all
    }

    fun getFolderFiles(folderId: Long): List<MediaEntity> {
        fileBox.query(MediaEntity_.folderId.equal(folderId)).build().use { return it.find() }
    }

    fun getFavourites(): List<FavouriteEntity> = favouriteBox.all

    fun getFile(fileId: Long): MediaEntity = fileBox[fileId]

    fun getFiles(fileIds: List<Long>): List<MediaEntity> = fileBox[fileIds]

    fun save(favourite: FavouriteEntity) {
        favouriteBox.put(favourite)
    }

    fun save(files: List<MediaEntity>) {
        fileBox.put(files)
    }

    fun save(folder: FolderEntity) {
        folderBox.put(folder)
    }

    fun updateFolderFiles(folderEntity: FolderEntity, exploredFiles: List<MediaEntity>) {
        val dbFiles = getFolderFiles(folderEntity.id)
        val deadFiles = dbFiles - exploredFiles.toSet()
        val newFiles = exploredFiles - dbFiles.toSet()
        Log.d(TAG, "updateFolderFiles: dead files $deadFiles")
        fileBox.remove(deadFiles)
        newFiles.forEach { file -> file.folderId = folderEntity.id }
        Log.d(TAG, "updateFolderFiles: new files $newFiles")
        fileBox.put(newFiles)
        if (folderEntity.items != exploredFiles.size) {
            folderEntity.items = exploredFiles.size
            folderBox.put(folderEntity)
        }
    }

    fun updateFolders(exploredFolders: Set<FolderEntity>) {
        val dbFolders = folderBox.all
        val deadFolders = mutableSetOf<FolderEntity>()
        val newFolders = exploredFolders.toMutableList()
        dbFolders.forEach { file ->
            exploredFolders.find(file::equals)?.let {
                // already in DB
                    exploredFile ->
                newFolders.remove(exploredFile)
            } ?: deadFolders.add(file)

        }
        folderBox.remove(deadFolders)
        folderBox.put(newFolders)
        dbFolders.addAll(newFolders)
        val folIds = dbFolders.map(FolderEntity::id)
        fileBox.query(MediaEntity_.folderId.notOneOf(folIds.toLongArray())).build().use {
            val deadFiles = it.find()
            Log.d(TAG, "updateFolders: dead files ${deadFiles.size} ${deadFiles.map { FileEntity::name }}")
            fileBox.remove(deadFiles)
        }
    }

    fun delete(files: List<MediaEntity>) {
        fileBox.remove(files)
    }

    fun delete(folder: FolderEntity) {
        folderBox.remove(folder)
    }

    fun delete(favourite: FavouriteEntity) {
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
}