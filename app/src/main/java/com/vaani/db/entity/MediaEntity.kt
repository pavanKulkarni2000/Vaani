package com.vaani.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "media",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["folderId"])]
)
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var name: String,
    var path: String,
    val isUri: Boolean,
    val isAudio: Boolean,
    val duration: Long,
    var playBackProgress: Float,
    val folderId: Long
)
