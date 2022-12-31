package com.vaani.db

import android.content.Context
import com.vaani.models.Favourite
import com.vaani.models.File
import com.vaani.models.Folder

object DB {
    lateinit var dbOperations: DBOperations
    fun init(context: Context, dbOperations: DBOperations) {
        this.dbOperations = dbOperations
        dbOperations.init(context)
    }

    fun getFolders(): List<Folder> = dbOperations.getFolders()
    fun getFolderMediaList(folder: Folder): List<File> = dbOperations.getFolderMediaList(folder)
    fun getFavourites(): List<File> = dbOperations.getFavourites()

    /*
    Create folder if not exists, else update fields
     */
    fun updateFolder(folder: Folder) = dbOperations.updateFolder(folder)
    fun updateFolderMediaList(folder: Folder, files: List<File>) = dbOperations.updateFolderMediaList(folder, files)
    fun updateFavourite(favourite: Favourite) = dbOperations.updateFavourite(favourite)
    fun deleteFolderList(deadFiles:List<Folder>) = dbOperations.deleteFolderList(deadFiles)
    fun deleteFavourite(favourite: Favourite) = dbOperations.deleteFavourite(favourite)
}