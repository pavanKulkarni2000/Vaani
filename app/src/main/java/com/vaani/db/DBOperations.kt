package com.vaani.db

import com.vaani.models.Favourite
import com.vaani.models.File
import com.vaani.models.Folder

interface DBOperations {

    fun init()
    fun getFolders(): List<Folder>
    fun getFolderMediaList(folder: Folder): List<File>
    fun getFavourites(): List<Favourite>
    suspend fun upsertFolderMediaList(folder: Folder, files: List<File>): List<File>
    suspend fun upsertFavourite(favourite: Favourite): Favourite
    suspend fun upsertFolders(folders: Set<Folder>) : List<Folder>
    fun deleteFavourite(favourite: Favourite)
    fun close()
}