package com.vaani.data.util

import com.vaani.data.model.FileType
import com.vaani.data.model.Folder
import com.vaani.data.model.Media
import java.util.*

interface AndroidGenericFileType<T> {
  suspend fun listFolder(folder: T): List<T>

  fun getMedia(androidFile: T, isAudio: Boolean): Media

  fun mimeType(file: T): FileType

  fun getFolder(file: T, count: Int): Folder

  fun getDuration(file: T): Long

  fun delete(file: T)

  suspend fun discoverRecursive(root: T): Map<Folder, List<Media>> {
    val result = LinkedHashMap<Folder, List<Media>>()
    val stack = Stack<T>()
    stack.add(root)
    while (stack.isNotEmpty()) {
      val mediaList = LinkedList<Media>()
      val folder = stack.pop()
      val list = listFolder(folder)
      list.forEach { file ->
        when (val mime = mimeType(file)) {
          FileType.DIR -> stack.add(file)
          FileType.AUDIO,
          FileType.VIDEO -> mediaList.add(getMedia(file, mime == FileType.AUDIO))
          FileType.OTHER -> {}
        }
      }
      if (mediaList.isNotEmpty()) {
        val folderObj = getFolder(folder, mediaList.size)
        result[folderObj] = mediaList
      }
    }
    return result
  }

  suspend fun listFolderMedia(folder: T): List<Media> {
    return listFolder(folder).mapNotNull {
      when (mimeType(it)) {
        FileType.AUDIO -> getMedia(it, true)
        FileType.VIDEO -> getMedia(it, false)
        else -> null
      }
    }
  }
}
