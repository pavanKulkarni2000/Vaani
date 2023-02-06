package com.vaani.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import com.vaani.models.File
import com.vaani.models.FileType
import com.vaani.models.Folder
import java.nio.file.Paths

object FileUtil {

    private const val primaryStorageRootPath = "/storage/emulated/0/"

    suspend fun updatePrimaryStorageList(): Map<Folder, List<File>> {
        return AndroidPath.discoverRecursive(Paths.get(primaryStorageRootPath))
    }

    suspend fun updateSecondaryStorageList(context: Context): Map<Folder, List<File>> {
        try {
            val externalCacheDirs: Array<java.io.File> = context.getExternalFilesDirs(null)
            for (file in externalCacheDirs) {
                if (Environment.isExternalStorageRemovable(file)) {
                    val path = file.path.split("/Android").toTypedArray()[0]
                    return AndroidPath.discoverRecursive(Paths.get(path))
                }
            }
        } catch (_: Exception) {
        }
        return emptyMap()
    }

    suspend fun updateAndroidFolderList(context: Context): Map<Folder, List<File>> {
        val data = DocumentFile.fromTreeUri(context, Uri.parse(androidFolderTreeUriStr("data")))!!
        return AndroidDocFile.discoverRecursive(data)
    }


    suspend fun getMediaInFolder(context: Context, folder: Folder): List<File> {
        return if (folder.isUri) {
            AndroidDocFile.listFolderMedia(DocumentFile.fromTreeUri(context, Uri.parse(folder.path))!!)
        } else {
            AndroidPath.listFolderMedia(Paths.get(folder.path))
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