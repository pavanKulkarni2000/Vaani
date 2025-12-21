package com.vaani.dal

import androidx.room.withTransaction
import com.vaani.db.AppDatabase
import com.vaani.db.FolderDao
import com.vaani.db.MediaDao
import com.vaani.db.entity.MediaEntity
import com.vaani.model.Folder
import com.vaani.model.Media
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaRepository(
    private val mediaDao: MediaDao,
    private val folderDao: FolderDao,
    private val db: AppDatabase
) {

    suspend fun getFolderFiles(folderId: Long): List<MediaEntity> = withContext(Dispatchers.IO) {
        mediaDao.getMediaForFolder(folderId)
    }

    suspend fun getFile(fileId: Long): MediaEntity? = withContext(Dispatchers.IO) {
        mediaDao.getMedia(fileId)
    }

    suspend fun getFiles(fileIds: List<Long>): List<MediaEntity> = withContext(Dispatchers.IO) {
        mediaDao.getMedia(fileIds)
    }

    suspend fun saveMedia(file: MediaEntity): Long = withContext(Dispatchers.IO) {
        mediaDao.insert(file)
    }

    suspend fun saveMedias(files: List<MediaEntity>) = withContext(Dispatchers.IO) {
        if(files.isNotEmpty()) {
            mediaDao.insertAll(files)
        }
    }

    suspend fun updateFolderFiles(folder: Folder, exploredFiles: List<Media>) = withContext(Dispatchers.IO) {
        db.withTransaction {
            val folderEntity = folderDao.getFolderByPath(folder.path) ?: return@withTransaction
            val currentMedia = mediaDao.getMediaForFolder(folderEntity.id)
            val exploredMediaPaths = exploredFiles.map { it.path }.toSet()

            // Create a list of media to remove, then remove them
            val mediaToDelete = currentMedia.filter { !exploredMediaPaths.contains(it.path) }
            if (mediaToDelete.isNotEmpty()) {
                mediaDao.delete(mediaToDelete.map { it.id })
            }

            val currentMediaPaths = currentMedia.map { it.path }.toSet()
            val mediaToAdd = exploredFiles
                .filter { !currentMediaPaths.contains(it.path) }
                .map {
                    MediaEntity(
                        name = it.name,
                        path = it.path,
                        isUri = it.isUri,
                        isAudio = it.isAudio,
                        duration = it.duration,
                        playBackProgress = it.playBackProgress,
                        folderId = folderEntity.id
                    )
                }
            if (mediaToAdd.isNotEmpty()) {
                mediaDao.insertAll(mediaToAdd)
            }
        }
    }

    suspend fun deleteMedia(file: MediaEntity) = withContext(Dispatchers.IO) {
        mediaDao.delete(file)
    }

    suspend fun deleteMedias(files: List<MediaEntity>) = withContext(Dispatchers.IO) {
        if (files.isNotEmpty()) mediaDao.delete(files.map { it.id })
    }
}
