package com.vaani.models

import androidx.annotation.DrawableRes

interface UiItem {
  val rank: Int
    get() = 0
  val id: Long
  val name: String
  val subTitle: String
  @get:DrawableRes
  val image: Int
  var selected: Boolean
}
