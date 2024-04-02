package com.vaani.data.util

import android.media.MediaMetadataRetriever
import android.util.Log
import com.vaani.data.model.FileType
import com.vaani.data.model.Folder
import com.vaani.data.model.Media
import com.vaani.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLConnection
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.deleteIfExists
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.streams.toList

object AndroidPath : AndroidGenericFileType<Path> {
  override suspend fun listFolder(folder: Path): List<Path> {
    return try {
      withContext(Dispatchers.IO) { Files.list(folder).toList() }
    } catch (e: AccessDeniedException) {
      emptyList()
    }
  }

  override fun mimeType(file: Path): FileType {
    if (file.isDirectory()) return FileType.DIR
    return FileUtil.fileType(URLConnection.guessContentTypeFromName(file.name))
  }

  override fun getMedia(androidFile: Path, isAudio: Boolean): Media {
    return Media(
      id=0,
      name = androidFile.fileName.toString(),
      isAudio = isAudio,
      path = androidFile.toString(),
      isUri = false,
      duration = getDuration(androidFile),
      playBackProgress = 0f,
      folderId = 0L
    )
  }

  override fun getFolder(file: Path, count: Int): Folder {
    return Folder(
      id=0,
      name = file.fileName.toString(),
      path = file.toString(),
      isUri = false,
    )
  }

  override fun getDuration(file: Path): Long {
    try {
      MediaMetadataRetriever().use {
        it.setDataSource(file.toString())
        val dur = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
        Log.d(TAG, "makeFile: duration $dur")
        return dur
      }
    } catch (e: Exception) {
      Log.e(TAG, "getDuration: failed for $file", e)
      return 0
    }
  }

  override fun delete(file: Path) {
    if (file.isDirectory()) {
      var subDirectoryDoesntExists = true
      for (subFile in Files.list(file)) {
        if (subFile.isDirectory()) {
          subDirectoryDoesntExists = false
        } else {
          Log.d(TAG, "delete: deleted ${subFile.name}")
          subFile.deleteIfExists()
        }
      }
      if (subDirectoryDoesntExists) {
        file.deleteIfExists()
      }
    } else {
      file.deleteIfExists()
    }
  }
}
