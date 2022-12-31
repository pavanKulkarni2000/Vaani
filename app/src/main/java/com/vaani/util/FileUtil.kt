package com.vaani.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import com.vaani.models.File
import com.vaani.models.FileType
import com.vaani.models.Folder
import java.nio.file.Paths

object FileUtil{

    private const val primaryStorageRootPath = "/storage/emulated/0/"

    private val androidPath = AndroidPath()

    private val androidDocFile = AndroidDocFile()

    suspend fun updatePrimaryStorageList(): List<Folder> {
        return androidPath.discoverRecursive(Paths.get(primaryStorageRootPath))
    }

    suspend fun updateSecondaryStorageList(context: Context): List<Folder> {
        try {
            val externalCacheDirs: Array<java.io.File> = context.getExternalFilesDirs(null)
            for (file in externalCacheDirs) {
                if (Environment.isExternalStorageRemovable(file)) {
                    val path = file.path.split("/Android").toTypedArray()[0]
                    return androidPath.discoverRecursive(Paths.get(path))
                }
            }
        } catch (_: Exception) {
        }
        return emptyList()
    }

    suspend fun updateAndroidFolderList(context: Context): List<Folder> {
        val data = DocumentFile.fromTreeUri(context, Uri.parse(androidFolderTreeUriStr("data")))!!
        return androidDocFile.discoverRecursive(data)
    }


    suspend fun getMediaInFolder(context: Context, folder: Folder): List<File> {
        return if (folder.isUri) {
            DocumentFile.fromTreeUri(context, Uri.parse(folder.path))?.let { androidDocFile.listFolderMedia(it) }
                ?: emptyList()
        } else {
            androidPath.listFolderMedia(Paths.get(folder.path))
        }
    }

    fun isVideoAudio(type: FileType): Boolean = type == FileType.AUDIO || type == FileType.VIDEO

    fun fileType(mimeType: String?): FileType {
        return mimeType?.let {
            try {
                FileType.valueOf(it.split("/")[0].uppercase())
            } catch (e: Exception) {
                FileType.OTHER
            }
        } ?: FileType.OTHER
    }

     fun androidFolderTreeUriStr(folder: String): String {
        return "content://com.android.externalstorage.documents/tree/primary%3AAndroid%2F$folder"
    }
}