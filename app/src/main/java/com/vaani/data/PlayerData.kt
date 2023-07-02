package com.vaani.data

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.vaani.models.FileEntity
import io.objectbox.annotation.Id

object PlayerData {
    fun setPlayList(playList: List<FileEntity>): List<MediaItem> {
        currentPlayList = playList
        return playList.map { MediaItem.Builder().setMediaId(it.path).build() }
    }

    fun getMetaData(currentMediaItemIndex: Int): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(currentPlayList[currentMediaItemIndex].name)
            .build()
    }

    private lateinit var currentPlayList: List<FileEntity>
}