package com.vaani.dal

import com.vaani.db.FolderDao
import com.vaani.db.entity.FolderEntity
import com.vaani.model.Folder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FolderRepository(private val folderDao: FolderDao) {

    suspend fun getFolders(): List<FolderEntity> = withContext(Dispatchers.IO) {
        folderDao.getAll()
    }

    suspend fun getFolderWithPath(path: String): FolderEntity? = withContext(Dispatchers.IO) {
        folderDao.getFolderByPath(path)
    }

    suspend fun save(folder: FolderEntity): Long = withContext(Dispatchers.IO) {
        folderDao.insert(folder)
    }

    suspend fun updateFolders(exploredFolders: Set<Folder>) = withContext(Dispatchers.IO) {
        val dbFolders = folderDao.getAll()
        val exploredFolderPaths = exploredFolders.map { it.path }.toSet()

        val deadFolders = dbFolders.filter { !exploredFolderPaths.contains(it.path) }
        for (folder in deadFolders) {
            folderDao.delete(folder)
        }

        val dbFolderPaths = dbFolders.map { it.path }.toSet()
        val newFolders = exploredFolders
            .filter { !dbFolderPaths.contains(it.path) }
            .map { FolderEntity(name = it.name, path = it.path, isUri = it.isUri) }
        for (folder in newFolders) {
            folderDao.insert(folder)
        }
    }

    suspend fun deleteFolder(folder: FolderEntity) = withContext(Dispatchers.IO) {
        folderDao.delete(folder)
    }

    suspend fun getFolder(id: Long): FolderEntity? = withContext(Dispatchers.IO) {
        folderDao.getFolder(id)
    }

    suspend fun searchFolders(query: String): List<FolderEntity> = withContext(Dispatchers.IO) {
        folderDao.searchFolders(query)
    }
}
