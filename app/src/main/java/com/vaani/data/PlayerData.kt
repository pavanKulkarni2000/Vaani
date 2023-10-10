package com.vaani.data

import com.vaani.models.MediaEntity

object PlayerData {

    fun setCurrent(folderId: Long,playList:List<MediaEntity>) {
        this.currentCollection = folderId
        this.currentPlayList = playList
    }

    var currentCollection: Long = 0
        private set
    var currentPlayList: List<MediaEntity> = emptyList()
        private set
}