package com.vaani.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import io.objectbox.relation.ToOne

@Entity
data class Favourite(
    @Id
    var id: Long = 0,
    var rank: Int = 0,
) {
    lateinit var file: ToOne<File>
}