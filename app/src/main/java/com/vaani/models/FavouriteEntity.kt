package com.vaani.models

import com.vaani.R
import com.vaani.ui.util.UiUtil
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import io.objectbox.annotation.Transient

@Entity
data class FavouriteEntity(
  @Id override var id: Long = 0,
  @Unique var fileId: Long = 0,
  override var name: String = "",
  var isAudio: Boolean = false,
  var duration: Long = 0,
  override var rank: Int = 0,
  @Transient
  override var selected: Boolean = false,
) : UiItem {
  constructor(
    mediaEntity: MediaEntity,
    rank: Int
  ) : this(0, mediaEntity.id, mediaEntity.name, mediaEntity.isAudio, mediaEntity.duration, rank)

  override val subTitle: String
    get() = UiUtil.stringToTime(duration)

  override val image: Int
    get() =
      if(selected)
        R.drawable.check_circle_40px
      else
        when (isAudio) {
          true -> R.drawable.music_note_40px
          false -> R.drawable.movie_40px
        }
}
