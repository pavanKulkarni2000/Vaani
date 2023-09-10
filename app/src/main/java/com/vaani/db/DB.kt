package com.vaani.db

import android.content.Context
import com.vaani.models.FavouriteEntity
import com.vaani.models.FileEntity
import com.vaani.models.FileEntity_
import com.vaani.models.FolderEntity
import com.vaani.models.FolderEntity_
import com.vaani.models.MyObjectBox
import io.objectbox.Box
import io.objectbox.BoxStore

object DB {

    private lateinit var store: BoxStore
    private lateinit var folderBox: Box<FolderEntity>
    private lateinit var fileBox: Box<FileEntity>
    private lateinit var favouriteBox: Box<FavouriteEntity>

    fun init(context: Context) {
        store = MyObjectBox.builder()
            .androidContext(context)
            .build()
        folderBox = store.boxFor(FolderEntity::class.java)
        fileBox = store.boxFor(FileEntity::class.java)
        favouriteBox = store.boxFor(FavouriteEntity::class.java)
    }

    /**
     * get all folders saved
     */
    fun getFolders(): List<FolderEntity> {
        return folderBox.all
    }

    /**
     * get folder by path
     */
    fun getFolder(path: String): FolderEntity? {
        folderBox.query(FolderEntity_.path.equal(path)).build().use { return it.findFirst() }
    }

    /**
     * get all files in the folder
     */
    fun getFolderFiles(folderId: Long): List<FileEntity> {
        fileBox.query(FileEntity_.folderId.equal(folderId)).build().use { return it.find() }
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
    }

    fun getFavourites(): List<FavouriteEntity> = favouriteBox.all


    fun insertFavourite(favouriteEntity: FavouriteEntity) {
        favouriteBox.put(favouriteEntity)
    }

    fun updateFavourites(allFavourites: List<FavouriteEntity>) {
        favouriteBox.put(allFavourites)
    }

    fun deleteFavourite(favouriteEntity: FavouriteEntity) {
        favouriteBox.remove(favouriteEntity)
    }

    fun close() {
        store.close()
    }

    fun save(file: FileEntity) {
        fileBox.put(file)
    }

    fun save(folder: FolderEntity) {
        folderBox.put(folder)
    }

    fun getFile(fileId: Long): FileEntity = fileBox[fileId]

    fun getFiles(fileIds: List<Long>): List<FileEntity> = fileBox[fileIds]
    fun resume(context: Context) {
        if (store.isClosed) {
            init(context)
        }
    }

    fun delete(file: FileEntity) {
        fileBox.remove(file)
    }

    fun delete(folder: FolderEntity) {
        folderBox.remove(folder)
    }

    fun delete(favourite: FavouriteEntity) {
        favouriteBox.remove(favourite)
    }
}