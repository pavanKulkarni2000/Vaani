package com.vaani.data

import com.vaani.data.model.Media
import com.vaani.db.entity.MediaEntity

object PlayerData {

  fun setCurrent(folderId: Long, playList: List<Media>) {
    this.currentCollection = folderId
    this.currentPlayList = playList
  }

  var currentCollection: Long = 0
    private set

  var currentPlayList: List<Media> = emptyList()
    private set
}
