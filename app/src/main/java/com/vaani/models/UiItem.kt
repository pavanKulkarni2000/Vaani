package com.vaani.models

import androidx.annotation.DrawableRes

abstract class UiItem {
  open val rank: Int = 0
  abstract val id: Long
  abstract val name: String
  abstract val subTitle: String
  @get:DrawableRes abstract val image: Int
}
