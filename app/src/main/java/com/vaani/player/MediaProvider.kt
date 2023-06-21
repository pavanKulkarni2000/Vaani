package com.vaani.player

import androidx.media3.common.MediaItem

object MediaProvider {
    val root: MediaItem
    get() = MediaItem.Builder()
        .setMediaId("")
}