package com.vaani.data.util

import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.vaani.data.model.FileType
import com.vaani.data.model.Folder
import com.vaani.data.model.Media
import com.vaani.ui.MainActivity
import com.vaani.db.entity.FolderEntity
import com.vaani.db.entity.MediaEntity
import com.vaani.util.Constants
import com.vaani.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AndroidDocFile : AndroidGenericFileType<DocumentFile> {
  override suspend fun listFolder(folder: DocumentFile): List<DocumentFile> {
    return withContext(Dispatchers.IO) { folder.listFiles().toList() }
  }

  override fun getMedia(androidFile: DocumentFile, isAudio: Boolean): Media {
    return Media(id=0,
      name = androidFile.name ?: Constants.UNNAMED_FILE,
      path = androidFile.uri.toString(),
      isUri = true,
      isAudio = isAudio,
      duration = getDuration(androidFile),
      playBackProgress = 0f,
      folderId = 0L
    )
  }

  override fun mimeType(file: DocumentFile): FileType {
    if (file.isDirectory) return FileType.DIR
    return FileUtil.fileType(file.type)
  }

  override fun getFolder(file: DocumentFile, count: Int): Folder {
    return Folder(
      id=0,
      name = file.name ?: Constants.UNNAMED_FILE,
      path = file.uri.toString(),
      isUri = true,
    )
  }

  override fun getDuration(file: DocumentFile): Long {
    try {
      MediaMetadataRetriever().use {
        it.setDataSource(MainActivity.context, file.uri)
        val dur = it.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0
        Log.d(TAG, "makeFile: duration $dur")
        return dur
      }
    } catch (e: Exception) {
      Log.e(TAG, "getDuration: failed for ${file.uri}", e)
      return 0
    }
  }

  override fun delete(file: DocumentFile) {
    if (file.isDirectory) {
      var noSubDirExist = true
      for (subFile in file.listFiles()) {
        if (subFile.isDirectory) {
          noSubDirExist = false
        } else {
          subFile.delete()
        }
      }
      if (noSubDirExist) {
        file.delete()
      }
    } else {
      file.delete()
    }
  }
}
