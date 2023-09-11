package com.vaani.data

import androidx.media3.common.MediaMetadata
import com.vaani.models.MediaEntity

object PlayerData {

    fun getMetaData(currentMediaItemIndex: Int): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(currentPlayList[currentMediaItemIndex].name)
            .build()
    }

    fun setCollectionId(folderId: Long) {
        this.currentCollection = folderId
        this.currentPlayList = ArrayList(Files.getCollectionFiles(folderId))
    }

    var currentCollection: Long = 0
    var currentPlayList: List<MediaEntity> = emptyList()
        private set
}