package com.vaani.db

object DB {
    lateinit var CRUD: DBOperations
        private set

    fun init(dbOperations: DBOperations) {
        this.CRUD = dbOperations
        CRUD.init()
    }

    fun close() {
        CRUD.close()
    }
}