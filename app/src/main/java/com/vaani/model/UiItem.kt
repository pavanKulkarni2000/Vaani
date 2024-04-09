package com.vaani.model

import androidx.annotation.DrawableRes

interface UiItem {
  val id: Long
  val name: String
  val subTitle: String
  @get:DrawableRes val image: Int
  val rank: Int
  var selected: Boolean
}
