package com.vaani.model

import com.vaani.R

data class Search(
  override val name: String,
  val type: ItemType
) : UiItem {
  override val id: Long
    get() = 0
  override val subTitle: String
    get() = ""

  override val image: Int
    get() = R.drawable.history_20px

  override val rank: Int
    get() = 0
  override var selected: Boolean
    get() = false
    set(value) {}

  constructor(id:Long,name:String,type:ItemType) : this(name,type)
}
