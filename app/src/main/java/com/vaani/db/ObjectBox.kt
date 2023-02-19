package com.vaani.db

import android.content.Context
import android.util.Log
import com.vaani.models.CollectionPreference
import com.vaani.models.CollectionPreference_
import com.vaani.models.Favourite
import com.vaani.models.Favourite_
import com.vaani.models.File
import com.vaani.models.File_
import com.vaani.models.Folder
import com.vaani.models.MyObjectBox
import com.vaani.models.PlayBack
import com.vaani.models.PlayBack_
import com.vaani.util.PreferenceUtil
import com.vaani.util.TAG
import io.objectbox.Box
import io.objectbox.BoxStore

object ObjectBox : DBOperations {

    private lateinit var store: BoxStore
    private lateinit var folderBox: Box<Folder>
    private lateinit var fileBox: Box<File>
    private lateinit var favouriteBox: Box<Favourite>
    private lateinit var playBackBox: Box<PlayBack>
    private lateinit var collectionPreferenceBox: Box<CollectionPreference>

    override fun init(context: Context) {
        store = MyObjectBox.builder()
            .androidContext(context)
            .build()
        folderBox = store.boxFor(Folder::class.java)
        fileBox = store.boxFor(File::class.java)
        favouriteBox = store.boxFor(Favourite::class.java)
        playBackBox = store.boxFor(PlayBack::class.java)
        collectionPreferenceBox = store.boxFor(CollectionPreference::class.java)
    }

    override fun getFolders(): List<Folder> {
        return folderBox.all.sortedBy(Folder::name)
    }

    override fun getFolderMediaList(folderId: Long): List<File> {
        if(folderId==-1L){
            return favouriteBox.all.sortedBy(Favourite::rank).map{it.file.target.apply { this.folderId = -1 }}
        }
        fileBox.query(File_.folderId.equal(folderId)).build().use { return it.find().sortedBy(File::name) }
    }

    override fun getFavourites(): List<Favourite> {
        return favouriteBox.all.sortedBy(Favourite::rank)
    }

    override fun getPlayback(fileId: Long): PlayBack? {
         playBackBox.query(PlayBack_.fileId.equal(fileId)).build().use { return it.findFirst() }
    }

    override fun getPlaybacks(): List<PlayBack> {
        return playBackBox.all
    }

    override fun getCollectionPreference(collectionId: Long): CollectionPreference? {
        if(collectionId==-1L){
            return PreferenceUtil.favPreference
        }
        collectionPreferenceBox.query(CollectionPreference_.collectionId.equal(collectionId))
            .build()
            .use {
                return it.findFirst() }
    }

    override suspend fun upsertFolderMediaList(folder: Folder, files: List<File>): List<File> {
        val dbFiles = getFolderMediaList(folder.id)
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
        val builder = favouriteBox.query()
            builder.link(Favourite_.file).apply(File_.id.equal(favourite.file.target.id));
            builder.build().use {
            if(it.count()==0L){
                throw Exception("upsertFavourite: File is already favourite")
            }
        }
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

    override fun upsertCollectionPreference(collectionPreference: CollectionPreference): CollectionPreference {
        Log.d(TAG, "upsertCollectionPreference: $collectionPreference")
        if(collectionPreference.collectionId==-1L){
            PreferenceUtil.Favourite.put(collectionPreference)
        }else {
            collectionPreferenceBox.put(collectionPreference)
        }
        return collectionPreference
    }

    override fun updatePlaybacks(files: Set<File>) {
        val existingFiles = files.map(File::id)
        val dbPlaybacks = playBackBox.all
        dbPlaybacks.removeIf { playback-> existingFiles.contains(playback.fileId) }
        playBackBox.remove(dbPlaybacks)
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
}