package com.vaani.data.util

import com.vaani.models.FileEntity
import com.vaani.models.FolderEntity
import java.util.*

interface AndroidGenericFileType<T> {
    suspend fun listFolder(folder: T): List<T>
    fun makeFile(androidFile: T, isAudio: Boolean): FileEntity
    fun mimeType(file: T): FileType
    fun makeFolder(file: T, count: Int): FolderEntity
    fun getDuration(file: T): Long

    suspend fun discoverRecursive(root: T): Map<FolderEntity, List<FileEntity>> {
        val result = LinkedHashMap<FolderEntity, List<FileEntity>>()
        val stack = Stack<T>()
        stack.add(root)
        while (stack.isNotEmpty()) {
            val mediaList = LinkedList<FileEntity>()
            val folder = stack.pop()
            val list = listFolder(folder)
            list.forEach { file ->
                when (val mime = mimeType(file)) {
                    FileType.DIR -> stack.add(file)
                    FileType.AUDIO, FileType.VIDEO -> mediaList.add(
                        makeFile(file, mime == FileType.AUDIO)
                    )
                    FileType.OTHER -> {}
                }
            }
            if (mediaList.isNotEmpty()) {
                val folderObj = makeFolder(folder, mediaList.size)
                result[folderObj] = mediaList
            }
        }
        return result
    }

    suspend fun listFolderMedia(folder: T): List<FileEntity> {
        return listFolder(folder).mapNotNull {
            when (mimeType(it)) {
                FileType.AUDIO -> makeFile(it, true)
                FileType.VIDEO -> makeFile(it, false)
                else -> null
            }
        }
    }
}