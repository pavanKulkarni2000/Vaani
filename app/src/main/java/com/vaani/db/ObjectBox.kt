package com.vaani.db
//
//import android.content.Context
//import android.util.Log
//import com.vaani.models.Favourite
//import com.vaani.models.File
//import com.vaani.models.File_
//import com.vaani.models.Folder
//import com.vaani.models.Folder_
//import com.vaani.models.MyObjectBox
//import com.vaani.util.TAG
//import io.objectbox.Box
//import io.objectbox.BoxStore
//
//object ObjectBox : DBOperations {
//
//    lateinit var store: BoxStore
//        private set
//
//    lateinit var folderBox: Box<Folder>
//    lateinit var fileBox: Box<File>
//    lateinit var favouriteBox: Box<Favourite>
//
//    override fun init(context: Context) {
//        store = MyObjectBox.builder()
//            .androidContext(context.applicationContext)
//            .build()
//        folderBox = store.boxFor(Folder::class.java)
//        fileBox = store.boxFor(File::class.java)
//        favouriteBox = store.boxFor(Favourite::class.java)
//    }
//
//    override fun getFolders(): List<Folder> {
//        folderBox.query(Folder_.id.notEqual(-1)).build().use {
//            Log.d(TAG, "getFolders: ${it.find()}")
//            return it.find()
//        }
//    }
//
//    override fun getFolderMediaList(folder: Folder): List<File> {
//        fileBox.query(File_.folderId.equal(folder.id)).build().use { return it.find() }
//    }
//
//    override fun getFavourites(): List<File> {
//        fileBox.query().`in`(File_.id, favouriteBox.all.map(Favourite::fileId).toLongArray()).build()
//            .use { return it.find() }
//    }
//
//    override fun updateFolder(folder: Folder) {
//        folderBox.put(folder)
//    }
//
//    override fun updateFolderMediaList(folder: Folder, files: List<File>) {
//        val dbFiles = getFolderMediaList(folder)
//        val newFiles = files - dbFiles.toSet()
//        val deadFiles = dbFiles - files.toSet()
//        newFiles.forEach { it.folderId = folder.id }
//        fileBox.put(newFiles)
//        fileBox.remove(deadFiles)
//    }
//
//    override fun updateFavourite(favourite: Favourite) {
//        favouriteBox.put(favourite)
//    }
//
//    override fun deleteFolderList(deadFiles: List<Folder>) {
//        folderBox.remove(deadFiles)
//    }
//
//    override fun deleteFavourite(favourite: Favourite) {
//        favouriteBox.remove(favourite)
//        val list = favouriteBox.all.apply { sortBy(Favourite::rank) }
//        for (i in favourite.rank until list.size) {
//            list[i].rank = i
//        }
//        favouriteBox.put(list.subList(favourite.rank, list.size - 1))
//    }
//}