package com.vaani.util

import android.util.Log
import com.vaani.db.DB
import com.vaani.models.File
import com.vaani.models.FileType
import com.vaani.models.Folder
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.collections.Map

interface AndroidGenericFileType<T> {
    suspend fun listFolder(folder: T): List<T>
    fun makeFile(androidFile: T, isAudio: Boolean): File
    fun mimeType(file: T): FileType
    fun makeFolder(file: T, count: Int): Folder

    suspend fun discoverRecursive(root: T): Map<Folder,List<File>> {
        val result = LinkedHashMap<Folder,List<File>>()
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
                val folderObj = makeFolder(folder, mediaList.size)
                result[folderObj] = mediaList
            }
        }
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