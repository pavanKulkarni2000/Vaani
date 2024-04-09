package com.vaani.player

import com.vaani.model.Media

object PlayerData {

  fun setCurrent(folderId: Long, playList: List<Media>) {
    currentCollection = folderId
    currentPlayList = playList
  }

  var currentCollection: Long = 0
    private set

  var currentPlayList: List<Media> = emptyList()
    private set
}
