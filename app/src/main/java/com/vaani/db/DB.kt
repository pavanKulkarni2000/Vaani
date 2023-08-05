package com.vaani.db

import android.content.Context
import com.vaani.models.FavouriteEntity
import com.vaani.models.FileEntity
import com.vaani.models.FileEntity_
import com.vaani.models.FolderEntity
import com.vaani.models.MyObjectBox
import io.objectbox.Box
import io.objectbox.BoxStore

object DB {

    private lateinit var store: BoxStore
    private lateinit var folderEntityBox: Box<FolderEntity>
    private lateinit var fileBox: Box<FileEntity>
    private lateinit var favouriteEntityBox: Box<FavouriteEntity>

    fun init(context: Context) {
        store = MyObjectBox.builder()
            .androidContext(context)
            .build()
        folderEntityBox = store.boxFor(FolderEntity::class.java)
        fileBox = store.boxFor(FileEntity::class.java)
        favouriteEntityBox = store.boxFor(FavouriteEntity::class.java)
    }

    /**
     * get all folders saved
     */
    fun getFolders(): List<FolderEntity> {
        return folderEntityBox.all.sortedBy(FolderEntity::name)
    }

    /**
     * get all files in the folder
     */
    fun getFolderFiles(folderId: Long): List<FileEntity> {
        fileBox.query(FileEntity_.folderId.equal(folderId)).build().use { return it.find().sortedBy(FileEntity::name) }
    }


    fun updateFolderFiles(folderEntity: FolderEntity, exploredFiles: List<FileEntity>) {
        val dbFiles = getFolderFiles(folderEntity.id)
        val deadFiles = mutableSetOf<FileEntity>()
        val newFiles = exploredFiles.toMutableList()
        dbFiles.forEach { file ->
            exploredFiles.find(file::equals)?.let {
                // already in DB
                    exploredFile ->
                newFiles.remove(exploredFile)
            } ?: deadFiles.add(file)
        }
        fileBox.remove(deadFiles)
        newFiles.forEach { file -> file.folderId = folderEntity.id }
        fileBox.put(newFiles)
        if (folderEntity.items != exploredFiles.size) {
            folderEntity.items = exploredFiles.size
            folderEntityBox.put(folderEntity)
        }
    }

    fun updateFolders(exploredFolders: Set<FolderEntity>) {
        val dbFolders = folderEntityBox.all
        val deadFolders = mutableSetOf<FolderEntity>()
        val newFolders = exploredFolders.toMutableList()
        dbFolders.forEach { file ->
            exploredFolders.find(file::equals)?.let {
                // already in DB
                    exploredFile ->
                newFolders.remove(exploredFile)
            } ?: deadFolders.add(file)

        }
        folderEntityBox.remove(deadFolders)
        folderEntityBox.put(newFolders)
    }

    fun getFavourites(): List<FavouriteEntity> = favouriteEntityBox.all

    fun getFavouriteFiles(): List<FileEntity> = fileBox.get(getFavourites().map(FavouriteEntity::fileId))

    fun insertFavourite(favouriteEntity: FavouriteEntity) {
        favouriteEntityBox.put(favouriteEntity)
    }

    fun updateFavourites(allFavourites: List<FavouriteEntity>) {
        favouriteEntityBox.put(allFavourites)
    }

    fun deleteFavourite(favouriteEntity: FavouriteEntity) {
        favouriteEntityBox.remove(favouriteEntity)
    }

    fun close() {
        store.close()
    }

    fun save(file: FileEntity) {
        fileBox.put(file)
    }

    fun save(folder: FolderEntity) {
        folderEntityBox.put(folder)
    }

    fun getFile(fileId: Long): FileEntity = fileBox[fileId]
    fun resume(context: Context) {
        if (store.isClosed) {
            init(context)
        }
    }
}