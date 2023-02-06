package com.vaani.db

import android.util.Log
import com.vaani.MainActivity
import com.vaani.models.Favourite
import com.vaani.models.File
import com.vaani.models.File_
import com.vaani.models.Folder
import com.vaani.models.MyObjectBox
import com.vaani.models.PlayBack
import com.vaani.models.PlayBack_
import com.vaani.util.TAG
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
    private lateinit var playBackBox: Box<PlayBack>

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

    override fun getPlayback(fileId: Long): PlayBack? {
         playBackBox.query(PlayBack_.fileId.equal(fileId)).build().use { return it.findFirst() }
    }

    override suspend fun upsertFolderMediaList(folder: Folder, files: List<File>): List<File> {
        val dbFiles = getFolderMediaList(folder)
        val deadFiles = mutableSetOf<File>()
        dbFiles.forEach{
            file-> val index = files.indexOf(file)
            if(index==-1){
                deadFiles.add(file)
            }else {
                files[index].id = file.id
            }

        }
//            Log.d(TAG, "upsertFolders: new : $newFiles")
//            Log.d(TAG, "upsertFolders: dead : $deadFiles")
//            Log.d(TAG, "upsertFolders: files : $dbFiles")
        fileBox.remove(deadFiles)
        fileBox.put(files)
        return (files).sortedBy(File::name)
    }

    override suspend fun upsertFavourite(favourite: Favourite): Favourite {
        favouriteBox.put(favourite)
        return favourite
    }

    override suspend fun upsertFolders(folders: Set<Folder>): List<Folder> {
        val dbFolders = folderBox.all
        val deadFolders = mutableSetOf<Folder>()
        Log.d(TAG, "upsertFolders: $folders")
        dbFolders.forEach{
                folder-> val index = folders.indexOf(folder)
            if(index==-1){
                deadFolders.add(folder)
            }else {
                folders.elementAt(index).id = folder.id
            }

        }
        Log.d(TAG, "upsertFolders: $folders")
//        Log.d(TAG, "upsertFolders: new : $newFolders")
//        Log.d(TAG, "upsertFolders: dead : $deadFolders")
//        Log.d(TAG, "upsertFolders: folders : $folders")
        folderBox.remove(deadFolders)
        folderBox.put(folders)
        return (folders).sortedBy(Folder::name)
    }

    override suspend fun upsertPlayback(playBack: PlayBack): PlayBack {
        playBackBox.put(playBack)
        return playBack
    }

    override fun deleteFavourite(favourite: Favourite) {
        favouriteBox.remove(favourite)
        val list = favouriteBox.all.apply { sortBy(Favourite::rank) }
        for (i in favourite.rank until list.size) {
            list[i].rank = i
        }
        favouriteBox.put(list.subList(favourite.rank, list.size - 1))
    }

    override fun deletePlayback(playBack: PlayBack) {
        playBackBox.remove(playBack)
    }

    override fun close() {
        store.close()
    }

    override fun upsertPlayBack(fileId: Long, position: Float) {
        getPlayback(fileId)?.let {
            playBackBox.put(it.apply { this.progress = position })
        }?:run{
            playBackBox.put(PlayBack(0,fileId,position,1f))
        }
    }
}