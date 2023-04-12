package com.vaani.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique

@Entity
data class PlayBack(
    @Id
    var id: Long = 0,
    @Unique
    var fileId: Long = 0,
    var progress: Float = 0F,
    var speed: Float = 1F,
    var loop: Boolean = false
) {
    constructor() : this(0, 0, 0F, 1F, false)
}
