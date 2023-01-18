package com.vaani.util

import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.vaani.MainActivity
import com.vaani.models.File
import com.vaani.models.FileType
import com.vaani.models.Folder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId

object AndroidDocFile : AndroidGenericFileType<DocumentFile> {
    override suspend fun listFolder(folder: DocumentFile): List<DocumentFile> {
        return withContext(Dispatchers.IO) {
            folder.listFiles().toList()
        }
    }

    override fun makeFile(androidFile: DocumentFile, isAudio: Boolean): File {
        var image : ByteArray? = null
        try {
            MediaMetadataRetriever().use {
                it.setDataSource(MainActivity.context, androidFile.uri)
                image = it.embeddedPicture
                Log.d(TAG, "makeFile: ${image.toString()}")
            }
        }catch (e:Exception){
            Log.e(TAG, "makeFile: exception",e )
        }

        return File().apply {
            this.name = androidFile.name ?: Constants.UNNAMED_FILE
            this.isAudio = isAudio
            this.path = androidFile.uri.toString()
            this.isUri = true
            this.image = image
        }
    }

    override fun mimeType(file: DocumentFile): FileType {
        if(file.isDirectory)
            return FileType.DIR
        return FileUtil.fileType(file.type)
    }

    override fun makeFolder(file: DocumentFile, count: Int): Folder {
        return Folder().apply {
            this.name = file.name ?: Constants.UNNAMED_FILE
            this.path = file.uri.toString()
            this.isUri = true
            this.items = count
        }
    }
}