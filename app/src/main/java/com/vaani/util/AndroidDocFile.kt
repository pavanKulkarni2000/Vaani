package com.vaani.util

import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.vaani.MainActivity
import com.vaani.models.File
import com.vaani.models.FileType
import com.vaani.models.Folder
import com.vaani.ui.player.Player
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object AndroidDocFile : AndroidGenericFileType<DocumentFile> {
    override suspend fun listFolder(folder: DocumentFile): List<DocumentFile> {
        return withContext(Dispatchers.IO) {
            folder.listFiles().toList()
        }
    }

    override fun makeFile(androidFile: DocumentFile, isAudio: Boolean): File {

        return File().apply {
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

    override fun makeFolder(file: DocumentFile, count: Int): Folder {
        return Folder(
            id = 0,
            name = file.name ?: Constants.UNNAMED_FILE,
            path = file.uri.toString(),
            isUri = true,
            items = count
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
        }catch (e:Exception){
            Log.e(TAG, "getDuration: error",e )
            return 0
        }
    }
}