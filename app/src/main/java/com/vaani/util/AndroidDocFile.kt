package com.vaani.util

import androidx.documentfile.provider.DocumentFile
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
        return File(
            name = androidFile.name ?: Constants.UNNAMED_FILE,
            isAudio = isAudio,
            path = androidFile.uri.toString(),
            isUri = true,
            folderId = ObjectId.invoke()
        )
    }

    override fun mimeType(file: DocumentFile): FileType {
        if(file.isDirectory)
            return FileType.DIR
        return FileUtil.fileType(file.type)
    }

    override fun makeFolder(file: DocumentFile, count: Int): Folder {
        return Folder(
            name = file.name ?: Constants.UNNAMED_FILE,
            path = file.uri.toString(),
            isUri = true,
            items = count
        )
    }
}