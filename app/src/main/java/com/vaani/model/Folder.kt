package com.vaani.model

import com.vaani.R

data class Folder(
  override val id: Long,
  override val name: String,
  val path: String,
  val isUri: Boolean,
  var mediaCount: Int = 0,
  var lastPlayedId: Long = 0,
  override var selected: Boolean = false,
) : UiItem {
  override val subTitle: String
    get() = String.format("%d media files", mediaCount)

  override val image: Int
    get() = if (selected) R.drawable.check_circle_40px else R.drawable.folders_folder_48px

  override val rank: Int
    get() = 0
}
