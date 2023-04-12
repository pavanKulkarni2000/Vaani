package com.vaani.db

import android.content.Context
import com.vaani.models.CollectionPreference
import com.vaani.models.Favourite
import com.vaani.models.File
import com.vaani.models.Folder
import com.vaani.models.PlayBack

interface DBOperations {

    fun init(context: Context)
    fun getFolders(): List<Folder>
    fun getFolderMediaList(folderId: Long): List<File>
    fun getFavourites(): List<Favourite>
    fun getPlayback(fileId: Long): PlayBack
    fun getPlaybacks(): List<PlayBack>
    fun getCollectionPreference(collectionId: Long): CollectionPreference
    suspend fun upsertFolderMediaList(folder: Folder, files: List<File>): List<File>
    suspend fun upsertFavourite(favourite: Favourite): Favourite
    suspend fun upsertFolders(folders: Set<Folder>): List<Folder>
    suspend fun upsertPlayback(playBack: PlayBack): PlayBack
    suspend fun updateFavourites(from: Int, to: Int): List<Favourite>
    fun upsertCollectionPreference(collectionPreference: CollectionPreference): CollectionPreference
    fun updatePlaybacks(files: Set<File>)
    fun deleteFavourite(favourite: Favourite)
    fun deletePlayback(playBack: PlayBack)
    fun close()
}