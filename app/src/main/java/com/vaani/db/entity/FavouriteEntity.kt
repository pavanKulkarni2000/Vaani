package com.vaani.db.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import io.objectbox.relation.ToOne

@Entity
data class FavouriteEntity(@Id var id: Long = 0, @Unique var rank: Int = 0) {
  lateinit var media: ToOne<MediaEntity>
}
