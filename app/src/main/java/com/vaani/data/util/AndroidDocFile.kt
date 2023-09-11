package com.vaani.data.util

import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.vaani.MainActivity
import com.vaani.models.FolderEntity
import com.vaani.models.MediaEntity
import com.vaani.util.Constants
import com.vaani.util.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AndroidDocFile : AndroidGenericFileType<DocumentFile> {
    override suspend fun listFolder(folder: DocumentFile): List<DocumentFile> {
        return withContext(Dispatchers.IO) {
            folder.listFiles().toList()
        }
    }

    override fun makeFile(androidFile: DocumentFile, isAudio: Boolean): MediaEntity {

        return MediaEntity().apply {
            this.name = androidFile.name ?: Constants.UNNAMED_FILE
            this.isAudio = isAudio
            this.path = androidFile.uri.toString()
            this.isUri = true
            this.duration = getDuration(androidFile)
        }
    }

    override fun mimeType(file: DocumentFile): FileType {
        if (file.isDirectory)
            return FileType.DIR
        return FileUtil.fileType(file.type)
    }

    override fun makeFolder(file: DocumentFile, count: Int): FolderEntity {
        return FolderEntity().apply {
            name = file.name ?: Constants.UNNAMED_FILE
            path = file.uri.toString()
            isUri = true
            items = count
        }
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