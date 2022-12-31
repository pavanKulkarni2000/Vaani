package com.vaani.util

import com.vaani.db.DB
import com.vaani.models.File
import com.vaani.models.FileType
import com.vaani.models.Folder
import java.util.*

interface AndroidGenericFileType<T> {
    suspend fun listFolder(folder: T): List<T>
    fun makeFile(androidFile: T, isAudio: Boolean): File
    fun mimeType(file: T): FileType
    fun makeFolder(file: T, count: Int): Folder

    suspend fun discoverRecursive(root: T): List<Folder> {
        val result = LinkedList<Folder>()
        val stack = Stack<T>()
        stack.add(root)
        while (stack.isNotEmpty()) {
            val mediaList = LinkedList<File>()
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
                val folderDBObj = makeFolder(folder, mediaList.size)
                DB.updateFolder(folderDBObj)
                DB.updateFolderMediaList(folderDBObj, mediaList)
                result.add(folderDBObj)
            }
        }
        DB.deleteFolderList(DB.getFolders() - result)
        return result
    }

    suspend fun listFolderMedia(folder: T): List<File> {
        return listFolder(folder).mapNotNull {
            when (mimeType(it)) {
                FileType.AUDIO -> makeFile(it, true)
                FileType.VIDEO -> makeFile(it, false)
                else -> null
            }
        }
    }
}