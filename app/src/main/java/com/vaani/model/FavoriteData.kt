package com.vaani.model

/**
 * A Data Transfer Object for carrying favorite data from the database layer.
 * This class has no UI-specific logic.
 */
data class FavoriteData(
    val id: Long,
    val fileId: Long,
    val name: String,
    val rank: Int,
    val isAudio: Boolean,
    val duration: Long
)
