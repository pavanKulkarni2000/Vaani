package com.vaani.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique

@Entity
data class FavouriteEntity(
    @Id
    var id: Long = 0,
    @Unique
    var fileId: Long = 0,
    var name: String = "",
    var isAudio: Boolean = false,
    var duration: Long = 0,
    var rank: Int = 0,
) {
    constructor(mediaEntity: MediaEntity, rank: Int) : this(
        0,
        mediaEntity.id,
        mediaEntity.name,
        mediaEntity.isAudio,
        mediaEntity.duration,
        rank
    )
}