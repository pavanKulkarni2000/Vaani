package com.vaani.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String,
    var path: String,
    val isUri: Boolean,
    var lastPlayedMediaId: Long? = null,
    var playBackShuffle: Boolean = false
)
