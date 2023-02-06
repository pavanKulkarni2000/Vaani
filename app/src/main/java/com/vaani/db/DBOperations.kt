package com.vaani.db

import com.vaani.models.Favourite
import com.vaani.models.File
import com.vaani.models.Folder
import com.vaani.models.PlayBack

interface DBOperations {

    fun init()
    fun getFolders(): List<Folder>
    fun getFolderMediaList(folder: Folder): List<File>
    fun getFavourites(): List<Favourite>
    fun getPlayback(fileId:Long): PlayBack?
    suspend fun upsertFolderMediaList(folder: Folder, files: List<File>): List<File>
    suspend fun upsertFavourite(favourite: Favourite): Favourite
    suspend fun upsertFolders(folders: Set<Folder>): List<Folder>
    suspend fun upsertPlayback(playBack: PlayBack) : PlayBack
    fun deleteFavourite(favourite: Favourite)
    fun deletePlayback(playBack: PlayBack)
    fun close()
    fun upsertPlayBack(fileId: Long, position: Float)
}