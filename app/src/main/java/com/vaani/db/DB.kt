package com.vaani.db

import android.content.Context

object DB {
    lateinit var CRUD: DBOperations
        private set

    fun init(dbOperations: DBOperations, context: Context) {
        this.CRUD = dbOperations
        CRUD.init(context)
    }

    fun close() {
        CRUD.close()
    }
}