package com.vaani.data.util

import android.media.MediaMetadataRetriever
import android.util.Log
import com.vaani.models.FileEntity
import com.vaani.models.FileType
import com.vaani.models.FolderEntity
import com.vaani.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLConnection
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.streams.toList

object AndroidPath : AndroidGenericFileType<Path> {
    override suspend fun listFolder(folder: Path): List<Path> {
        return try {
            withContext(Dispatchers.IO) {
                Files.list(folder).toList()
            }
        } catch (e: AccessDeniedException) {
            emptyList()
        }
    }

    override fun mimeType(file: Path): FileType {
        if (file.isDirectory())
            return FileType.DIR
        return FileUtil.fileType(URLConnection.guessContentTypeFromName(file.name))
    }

    override fun makeFile(androidFile: Path, isAudio: Boolean): FileEntity {
        return FileEntity().apply {
            this.name = androidFile.fileName.toString()
            this.isAudio = isAudio
            this.path = androidFile.toString()
            this.isUri = false
            this.duration = getDuration(androidFile)
        }
    }

    override fun makeFolder(file: Path, count: Int): FolderEntity {
        return FolderEntity(
            id = 0,
            name = file.fileName.toString(),
            path = file.toString(),
            isUri = false,
            items = count,
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
            Log.e(TAG, "getDuration: error", e)
            return 0
        }
    }
}