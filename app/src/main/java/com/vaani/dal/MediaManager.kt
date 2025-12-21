package com.vaani.dal

import com.vaani.model.Media
import com.vaani.util.Constants
import com.vaani.util.PreferenceUtil
import com.vaani.util.toMedia
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaManager(
    private val folderRepository: FolderRepository,
    private val mediaRepository: MediaRepository
) {

    suspend fun getFolderMedias(id: Long): List<Media> = withContext(Dispatchers.IO) {
        mediaRepository.getFolderFiles(id).map { it.toMedia() }
    }

    suspend fun updateLastPlayedItems(folderId: Long, lastPlayedId: Long) = withContext(Dispatchers.IO) {
        when (folderId) {
            Constants.FAVOURITE_COLLECTION_ID -> PreferenceUtil.lastPlayedFavouriteId = lastPlayedId
            else -> {
                folderRepository.getFolder(folderId)?.let {
                    it.lastPlayedMediaId = lastPlayedId
                    folderRepository.save(it)
                }
            }
        }
        PreferenceUtil.lastPlayedFolderId = folderId
    }

    suspend fun saveProgress(media: Media) = withContext(Dispatchers.IO) {
        mediaRepository.getFile(media.id)?.let {
            it.playBackProgress = media.playBackProgress
            mediaRepository.saveMedia(it)
        }
    }
}
