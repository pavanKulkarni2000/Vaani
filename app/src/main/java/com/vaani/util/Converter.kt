package com.vaani.util

import com.vaani.db.entity.FavouriteEntity
import com.vaani.db.entity.FolderEntity
import com.vaani.db.entity.MediaEntity
import com.vaani.model.Favorite
import com.vaani.model.Folder
import com.vaani.model.ItemType
import com.vaani.model.Media
import com.vaani.model.Search

fun FolderEntity.toFolder() =
  Folder(
    id = id,
    name = name,
    path = path,
    isUri = isUri,
    mediaCount = medias.size,
    lastPlayedId = lastPlayedMedia?.target?.id ?: 0,
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
    folderId = folder.targetId,
    playBackProgress = playBackProgress,
    selected = false,
  )

fun FavouriteEntity.toFavourite() =
  Favorite(
    id = id,
    fileId = media.targetId,
    name = media.target.name,
    rank = rank,
    isAudio = media.target.isAudio,
    duration = media.target.duration,
    selected = false,
  )

fun FolderEntity.toSearch() = Search(id=id,name=name,type=ItemType.FOLDER)