package com.vaani.db.entity

import com.vaani.R
import com.vaani.ui.util.UiUtil
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import io.objectbox.annotation.Transient
import io.objectbox.relation.ToOne

@Entity
data class FavouriteEntity(
  @Id var id: Long = 0,
  @Unique var rank: Int = 0,
) {
  lateinit var media: ToOne<MediaEntity>
}
