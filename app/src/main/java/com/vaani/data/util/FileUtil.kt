package com.vaani.data.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.net.toFile
import androidx.documentfile.provider.DocumentFile
import com.vaani.models.FileEntity
import com.vaani.models.FolderEntity
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists

object FileUtil {

    private const val primaryStorageRootPath = "/storage/emulated/0/"

    private fun getSecondaryStorages(context: Context): List<String> {
        try {
            val externalCacheDirs: Array<java.io.File> = context.getExternalFilesDirs(null)
            val list = mutableListOf<String>()
            for (file in externalCacheDirs) {
                if (Environment.isExternalStorageRemovable(file)) {
                    val path = file.path.split("Android").toTypedArray()[0]
                    list.add(path)
                }
            }
            return list
        } catch (_: Exception) {
        }
        return mutableListOf()
    }

    suspend fun updatePrimaryStorageList(): Map<FolderEntity, List<FileEntity>> {
        return AndroidPath.discoverRecursive(Paths.get(primaryStorageRootPath))
    }

    suspend fun updateSecondaryStorageList(context: Context): Map<FolderEntity, List<FileEntity>> {
        val map = mutableMapOf<FolderEntity, List<FileEntity>>()
        getSecondaryStorages(context).forEach { path -> map += AndroidPath.discoverRecursive(Paths.get(path)) }
        return map
    }

    suspend fun updateAndroidFolderList(context: Context): Map<FolderEntity, List<FileEntity>> {
        val data = DocumentFile.fromTreeUri(context, Uri.parse(androidFolderTreeUriStr("data")))!!
        return AndroidDocFile.discoverRecursive(data)
    }


    suspend fun getMediaInFolder(context: Context, folderEntity: FolderEntity): List<FileEntity> {
        return if (folderEntity.isUri) {
            AndroidDocFile.listFolderMedia(DocumentFile.fromTreeUri(context, Uri.parse(folderEntity.path))!!)
        } else {
            AndroidPath.listFolderMedia(Paths.get(folderEntity.path))
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

    fun getPath(context: Context, uri: Uri?): Path? {
        var path: Path? = null
        uri?.let { _uri ->
            _uri.path?.let { _path ->
                if (_path.contains("/tree/primary:")) {
                    path = Paths.get(_path.replace("/tree/primary:", primaryStorageRootPath))
                } else {
                    val externalPaths = getSecondaryStorages(context)
                    for (externalPath in externalPaths) {
                        val sdCardName = externalPath.removePrefix("/storage/").removeSuffix("/")
                        if (_path.contains(sdCardName)) {
                            path = Paths.get(_path.replace("/tree/${sdCardName}:", externalPath))
                            break
                        }
                    }
                }
            }
        }
        if (path?.exists() != true) {
            path = null
        }
        return path
    }

    fun copyFile(sourceFile: FileEntity, destinationPath: Path) {
        if (sourceFile.isUri) {
            val file = Uri.parse(sourceFile.path).toFile()
            Files.copy(file.inputStream(), destinationPath, StandardCopyOption.REPLACE_EXISTING)
        } else {
            Files.copy(Paths.get(sourceFile.path), destinationPath, StandardCopyOption.REPLACE_EXISTING)
        }
    }
}