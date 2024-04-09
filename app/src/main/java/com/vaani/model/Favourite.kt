package com.vaani.model

import com.vaani.R
import com.vaani.ui.util.UiUtil

data class Favourite(
  override val id: Long,
  val fileId: Long,
  override val name: String,
  override val rank: Int,
  val isAudio: Boolean,
  val duration: Long,
  override var selected: Boolean = false,
) : UiItem {
  override val subTitle: String
    get() = UiUtil.stringToTime(duration)

  override val image: Int
    get() =
      if (selected) R.drawable.check_circle_40px
      else
        when (isAudio) {
          true -> R.drawable.music_note_40px
          false -> R.drawable.movie_40px
        }
}
