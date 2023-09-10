package com.vaani.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Unique

@Entity
data class FavouriteEntity(
    @Unique
    var fileId: Long = 0,
    var name: String = "",
    var isAudio: Boolean = false,
    var duration: Long = 0,
    var rank: Int = 0,
) : BaseEntity() {
    constructor(fileEntity: FileEntity, rank: Int) : this(
        fileEntity.id,
        fileEntity.name,
        fileEntity.isAudio,
        fileEntity.duration,
        rank
    )
}