package com.vaani.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique

@Entity
data class Favourite(
    @Id
    var id:Long = 0,
    @Unique
    var fileId:Long = 0,
    @Unique
    var rank: Int =0
)