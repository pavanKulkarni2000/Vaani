package com.vaani.dal

import com.vaani.db.DB
import com.vaani.db.entity.MediaEntity
import com.vaani.model.Media
import com.vaani.util.Constants
import com.vaani.util.PreferenceUtil
import com.vaani.util.toMedia

object Medias {

    fun getFolderMedias(id: Long): List<Media> {
        return DB.getFolderFiles(id).map(MediaEntity::toMedia)
    }

    fun updateLastPlayedItems(folderId: Long, lastPlayedId: Long) {
        when (folderId) {
            Constants.FAVOURITE_COLLECTION_ID -> PreferenceUtil.lastPlayedFavouriteId = lastPlayedId
            else -> {
                DB.getFolder(folderId).let {
                    it.lastPlayedMedia?.targetId = lastPlayedId
                    DB.save(it)
                }
            }
        }
        PreferenceUtil.lastPlayedFolderId = folderId
    }

    fun saveProgress(media: Media) {
        val dbMedia = DB.getFile(media.id)
        dbMedia.playBackProgress = media.playBackProgress
        DB.saveMedia(dbMedia)
    }
}