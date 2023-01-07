package com.vaani.db

import android.content.Context
import com.vaani.models.Favourite
import com.vaani.models.File
import com.vaani.models.Folder

object DB {
    lateinit var CRUD: DBOperations
    private set
    fun init(context: Context, dbOperations: DBOperations) {
        this.CRUD = dbOperations
        CRUD.init(context)
    }
}