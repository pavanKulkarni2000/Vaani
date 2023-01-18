package com.vaani.db

import com.vaani.MainActivity
import com.vaani.models.Favourite
import com.vaani.models.File
import com.vaani.models.File_
import com.vaani.models.Folder
import com.vaani.models.MyObjectBox
import io.objectbox.Box
import io.objectbox.BoxStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object ObjectBox : DBOperations {

    private lateinit var store: BoxStore
    private val scope = CoroutineScope(Dispatchers.IO)
    private lateinit var folderBox: Box<Folder>
    private lateinit var fileBox: Box<File>
    private lateinit var favouriteBox: Box<Favourite>

    override fun init() {
        store = MyObjectBox.builder()
            .androidContext(MainActivity.context)
            .build()
        folderBox = store.boxFor(Folder::class.java)
        fileBox = store.boxFor(File::class.java)
        favouriteBox = store.boxFor(Favourite::class.java)
    }

    override fun getFolders(): List<Folder> {
       return folderBox.all.sortedBy(Folder::name)
    }

    override fun getFolderMediaList(folder: Folder): List<File> {
        fileBox.query(File_.folderId.equal(folder.id)).build().use { return it.find().sortedBy(File::name) }
    }

    override fun getFavourites(): List<Favourite> {
        return favouriteBox.all.sortedBy(Favourite::rank)
    }

    override suspend fun upsertFolderMediaList(folder: Folder, files: List<File>): List<File> {
            val dbFiles = getFolderMediaList(folder)
            val newFiles = files - dbFiles.toSet()
            val deadFiles = dbFiles - files.toSet()
//            Log.d(TAG, "upsertFolders: new : $newFiles")
//            Log.d(TAG, "upsertFolders: dead : $deadFiles")
//            Log.d(TAG, "upsertFolders: files : $dbFiles")
            fileBox.remove(deadFiles)
            fileBox.put(newFiles)
            return (dbFiles+newFiles).sortedBy(File::name)
    }

    override suspend fun upsertFavourite(favourite: Favourite): Favourite {
        favouriteBox.put(favourite)
        return favourite
    }

    override suspend fun upsertFolders(folders: Set<Folder>): List<Folder> {
        val dbFolders = folderBox.all
        val deadFolders = dbFolders - folders
        val newFolders = folders - dbFolders.toSet()
//        Log.d(TAG, "upsertFolders: new : $newFolders")
//        Log.d(TAG, "upsertFolders: dead : $deadFolders")
//        Log.d(TAG, "upsertFolders: folders : $folders")
        folderBox.remove(deadFolders)
        folderBox.put(newFolders)
        return (dbFolders + newFolders).sortedBy(Folder::name)
    }

    override fun deleteFavourite(favourite: Favourite) {
        favouriteBox.remove(favourite)
        val list = favouriteBox.all.apply { sortBy(Favourite::rank) }
        for (i in favourite.rank until list.size) {
            list[i].rank = i
        }
        favouriteBox.put(list.subList(favourite.rank, list.size - 1))
    }

    override fun close() {
        store.close()
    }
}