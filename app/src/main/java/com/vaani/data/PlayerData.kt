package com.vaani.data

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.vaani.models.FileEntity

object PlayerData {
    fun getMediaItems(): List<MediaItem> {
        return currentPlayList.map { MediaItem.Builder().setMediaId(it.path).build() }
    }

    fun getMetaData(currentMediaItemIndex: Int): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(currentPlayList[currentMediaItemIndex].name)
            .build()
    }

    fun setCollectionId(folderId: Long) {
        this.currentCollection = folderId
        this.currentPlayList = Files.getFolderFiles(folderId)
    }

    var currentCollection: Long = 0
    var currentPlayList: List<FileEntity> = emptyList()
        private set
}