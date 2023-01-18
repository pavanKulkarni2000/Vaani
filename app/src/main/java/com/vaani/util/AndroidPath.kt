package com.vaani.util

import android.media.MediaMetadataRetriever
import android.util.Log
import com.bumptech.glide.Glide
import com.vaani.MainActivity
import com.vaani.models.File
import com.vaani.models.FileType
import com.vaani.models.Folder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
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

    override fun makeFile(androidFile: Path, isAudio: Boolean): File {
        var image : ByteArray?
        try {
            MediaMetadataRetriever().use {
                it.setDataSource(androidFile.toString())
                image = it.embeddedPicture
                Log.d(TAG, "makeFile: ${image.toString()}")
            }
        }catch (e:Exception){
            Log.e(TAG, "makeFile: exception",e )
        }

        return File().apply {
            this.name = androidFile.fileName.toString()
            this.isAudio = isAudio
            this.path = androidFile.toString()
            this.isUri = false
        }
    }

    override fun makeFolder(file: Path, count: Int): Folder {
        return Folder().apply {
            this.name = file.fileName.toString()
            this.path = file.toString()
            this.isUri = false
            this.items = count
        }
    }
}