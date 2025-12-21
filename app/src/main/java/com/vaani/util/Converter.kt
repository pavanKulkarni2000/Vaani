package com.vaani.util

import com.vaani.db.entity.FavouriteEntity
import com.vaani.db.entity.FolderEntity
import com.vaani.db.entity.MediaEntity
import com.vaani.model.Favorite
import com.vaani.model.FavoriteData
import com.vaani.model.Folder
import com.vaani.model.ItemType
import com.vaani.model.Media
import com.vaani.model.Search

fun FolderEntity.toFolder(mediaCount: Int) =
    Folder(
        id = id,
        name = name,
        path = path,
        isUri = isUri,
        mediaCount = mediaCount, // Provided by caller
        lastPlayedId = lastPlayedMediaId ?: 0,
        selected = false,
    )

fun MediaEntity.toMedia() =
    Media(
        id = id,
        name = name,
        path = path,
        isUri = isUri,
        isAudio = isAudio,
        duration = duration,
        folderId = folderId, // Use the ID directly
        playBackProgress = playBackProgress,
        selected = false,
    )

fun FavouriteEntity.toFavourite(media: MediaEntity) =
    Favorite(
        id = id,
        fileId = mediaId, // Use the ID directly
        name = media.name,
        rank = rank,
        isAudio = media.isAudio,
        duration = media.duration,
        selected = false,
    )

fun FavoriteData.toFavorite() =
    Favorite(
        id = id,
        fileId = fileId,
        name = name,
        rank = rank,
        isAudio = isAudio,
        duration = duration,
        selected = false,
    )

fun FolderEntity.toSearch() = Search(id = id, name = name, type = ItemType.FOLDER)
