package com.vaani.db

import android.content.Context
import com.vaani.models.Favourite
import com.vaani.models.File
import com.vaani.models.Folder

interface DBOperations {

    fun init(context: Context)
    fun getFolders(): List<Folder>
    fun getFolderMediaList(folder: Folder): List<File>
    fun getFavourites(): List<File>
    fun updateFolder(folder: Folder)
    fun updateFolderMediaList(folder: Folder, files: List<File>)
    fun updateFavourite(favourite: Favourite)
    fun deleteFolderList(deadFiles:List<Folder>)
    fun deleteFavourite(favourite: Favourite)
}