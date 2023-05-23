package com.vaani.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class FavouriteEntity(
    @Id
    var id: Long = 0,
    var fileId: Long = 0,
    var rank: Int = 0,
)