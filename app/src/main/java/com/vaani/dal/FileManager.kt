package com.vaani.dal

import android.content.Context
import com.vaani.db.entity.FolderEntity
import com.vaani.db.entity.MediaEntity
import com.vaani.model.Folder
import com.vaani.model.Media
import com.vaani.model.Search
import com.vaani.util.FileUtil
import com.vaani.util.toFolder
import com.vaani.util.toMedia
import com.vaani.util.toSearch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class FileManager(
    private val context: Context,
    private val folderRepository: FolderRepository,
    private val mediaRepository: MediaRepository,
    private val favoritesRepository: FavoritesRepository
) {
    suspend fun getFolders(): List<Folder> = withContext(Dispatchers.IO) {
        val folders = folderRepository.getFolders()
        folders.map { folder ->
            val mediaCount = mediaRepository.getFolderFiles(folder.id).size
            folder.toFolder(mediaCount)
        }
    }

    suspend fun searchFolders(query: String): List<Search> = withContext(Dispatchers.IO) {
        folderRepository.searchFolders(query).map { it.toSearch() }
    }

    suspend fun exploreFolders() {
        coroutineScope {
            val differed1 = async { FileUtil.updatePrimaryStorageList() }
            val differed2 = async { FileUtil.updateSecondaryStorageList(context) }
            val differed3 = async { FileUtil.updateAndroidFolderList(context) }
            val folderMedias = (differed1.await() + differed2.await() + differed3.await())
            folderRepository.updateFolders(folderMedias.keys)
            folderMedias.forEach { (folder, mediaList) -> mediaRepository.updateFolderFiles(folder, mediaList) }
        }
    }

    suspend fun exploreFolder(folder: Folder) {
        val mediaList =
            withContext(Dispatchers.IO) { FileUtil.getMediaInFolder(context, folder) }
        mediaRepository.updateFolderFiles(folder, mediaList)
    }

    suspend fun getMedias(fileIds: List<Long>): List<Media> = withContext(Dispatchers.IO) {
        mediaRepository.getFiles(fileIds).map { it.toMedia() }
    }

    suspend fun deleteMedia(file: MediaEntity) = withContext(Dispatchers.IO) {
        FileUtil.delete(file)
        mediaRepository.deleteMedia(file)
        folderRepository.getFolder(file.folderId)?.let { folder ->
            val mediaCount = mediaRepository.getFolderFiles(folder.id).size
            if (mediaCount == 0) {
                folderRepository.deleteFolder(folder)
            }
        }
        favoritesRepository.getAll().first().find { fav -> fav.fileId == file.id }?.let { favoritesRepository.remove(it) }
    }

    suspend fun deleteFolder(folder: FolderEntity) = withContext(Dispatchers.IO) {
        FileUtil.delete(folder)
        val medias = mediaRepository.getFolderFiles(folder.id)
        mediaRepository.deleteMedias(medias)
        val mediaIds = medias.map { it.id }.toSet()
        favoritesRepository.getAll().first().forEach {
            if (mediaIds.contains(it.fileId)) {
                favoritesRepository.remove(it)
            }
        }
        folderRepository.deleteFolder(folder)
    }

    suspend fun renameFolder(folder: FolderEntity, newName: String) = withContext(Dispatchers.IO) {
        FileUtil.rename(folder, newName)
        folderRepository.save(folder)
    }

    suspend fun renameFile(mediaEntity: MediaEntity, newName: String) = withContext(Dispatchers.IO) {
        FileUtil.rename(mediaEntity, newName)
        mediaRepository.saveMedia(mediaEntity)
    }
}
