package com.vaani.util

import com.vaani.models.File
import com.vaani.models.FileType
import com.vaani.models.Folder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URLConnection
import java.nio.file.AccessDeniedException
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isDirectory
import kotlin.io.path.name
import kotlin.streams.toList

class AndroidPath : AndroidGenericFileType<Path> {
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
        return File(
            name = androidFile.fileName.toString(),
            isAudio = isAudio,
            path = androidFile.toString(),
            isUri = false
        )
    }

    override fun makeFolder(file: Path, count: Int): Folder {
        return Folder(name = file.fileName.toString(), path = file.toString(), isUri = false, items = count)
    }
}