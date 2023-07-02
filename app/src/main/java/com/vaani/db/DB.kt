package com.vaani.db

import android.content.Context
import android.util.Log
import com.vaani.models.FavouriteEntity
import com.vaani.models.FavouriteEntity_
import com.vaani.models.FileEntity
import com.vaani.models.FileEntity_
import com.vaani.models.FolderEntity
import com.vaani.models.MyObjectBox
import com.vaani.util.Constants.FAVOURITE_COLLECTION_ID
import com.vaani.util.TAG
import io.objectbox.Box
import io.objectbox.BoxStore
import java.util.stream.Collectors

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


    fun updateFolderFiles(folderEntity: FolderEntity, files: List<FileEntity>): List<FileEntity> {
        val dbFiles = getFolderFiles(folderEntity.id)
        val deadFiles = mutableSetOf<FileEntity>()
        dbFiles.forEach { file ->
            val index = files.indexOf(file)
            if (index == -1) {
                deadFiles.add(file)
            } else {
                files[index].id = file.id
            }

        }
//            Log.d(TAG, "upsertFolders: new : $newFiles")
//            Log.d(TAG, "upsertFolders: dead : $deadFiles")
//            Log.d(TAG, "upsertFolders: files : $dbFiles")
        fileBox.remove(deadFiles)
        fileBox.put(files)
        return (files).sortedBy(FileEntity::name)
    }

    fun updateFolders(folderEntities: Set<FolderEntity>): List<FolderEntity> {
        val dbFolders = folderEntityBox.all
        val deadFolderEntities = mutableSetOf<FolderEntity>()
        Log.d(TAG, "upsertFolders: $folderEntities")
        dbFolders.forEach { folder ->
            val index = folderEntities.indexOf(folder)
            if (index == -1) {
                deadFolderEntities.add(folder)
            } else {
                folderEntities.elementAt(index).id = folder.id
            }

        }
        Log.d(TAG, "upsertFolders: $folderEntities")
//        Log.d(TAG, "upsertFolders: new : $newFolders")
//        Log.d(TAG, "upsertFolders: dead : $deadFolders")
//        Log.d(TAG, "upsertFolders: folders : $folders")
        folderEntityBox.remove(deadFolderEntities)
        folderEntityBox.put(folderEntities)
        return (folderEntities).sortedBy(FolderEntity::name)
    }

    fun getFavourites(): List<FavouriteEntity> = favouriteEntityBox.all

    fun getFavouriteFiles(): List<FileEntity> {
        val favs = favouriteEntityBox.all.sortedBy(FavouriteEntity::rank)
        val favMap = favs.stream().collect(Collectors.toMap(FavouriteEntity::fileId, FavouriteEntity::rank))
        val files = fileBox.get(favs.map(FavouriteEntity::fileId))
        files.forEach{file->file.folderId = FAVOURITE_COLLECTION_ID}
        return files.sortedBy { file -> favMap[file.id] }
    }

    fun getFavourite(fileId: Long): FavouriteEntity {
        favouriteEntityBox.query(FavouriteEntity_.fileId.equal(fileId)).build().use { return it.findFirst()!! }
    }


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
}