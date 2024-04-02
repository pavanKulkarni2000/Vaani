package com.vaani.data.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.documentfile.provider.DocumentFile
import com.vaani.data.model.FileType
import com.vaani.data.model.Folder
import com.vaani.data.model.Media
import com.vaani.db.entity.FileEntity
import com.vaani.ui.MainActivity
import java.nio.file.Path
import java.nio.file.Paths
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
    } catch (_: Exception) {}
    return mutableListOf()
  }

  suspend fun updatePrimaryStorageList(): Map<Folder, List<Media>> {
    return AndroidPath.discoverRecursive(Paths.get(primaryStorageRootPath))
  }

  suspend fun updateSecondaryStorageList(context: Context): Map<Folder, List<Media>> {
    val map = mutableMapOf<Folder, List<Media>>()
    getSecondaryStorages(context).forEach { path ->
      map += AndroidPath.discoverRecursive(Paths.get(path))
    }
    return map
  }

  suspend fun updateAndroidFolderList(context: Context): Map<Folder, List<Media>> {
    val data = DocumentFile.fromTreeUri(context, Uri.parse(androidFolderTreeUriStr("data")))!!
    return AndroidDocFile.discoverRecursive(data)
  }

  suspend fun getMediaInFolder(context: Context, folderEntity: Folder): List<Media> {
    return if (folderEntity.isUri) {
      AndroidDocFile.listFolderMedia(
        DocumentFile.fromTreeUri(context, Uri.parse(folderEntity.path))!!
      )
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

  fun getPath(uri: Uri): Path? {
    var path: Path? = null
    uri.path?.let { _path ->
      if (_path.contains("/tree/primary:")) {
        path = Paths.get(_path.replace("/tree/primary:", primaryStorageRootPath))
      } else {
        val externalPaths = getSecondaryStorages(MainActivity.context)
        for (externalPath in externalPaths) {
          val sdCardName = externalPath.removePrefix("/storage/").removeSuffix("/")
          if (_path.contains(sdCardName)) {
            path = Paths.get(_path.replace("/tree/${sdCardName}:", externalPath))
            break
          }
        }
      }
    }
    if (path?.exists() != true) {
      path = null
    }
    return path
  }

//  fun copyFile(sourceFile: Media, destinationUri: Uri): Media {
//    val newFile =
//      Media().apply {
//        name = sourceFile.name
//        isUri = false
//        isAudio = sourceFile.isAudio
//        duration = sourceFile.duration
//      }
//    getPath(destinationUri)?.let { path ->
//      val targetPath = path.resolve(sourceFile.name)
//      if (sourceFile.isUri) {
//        Files.copy(
//          MainActivity.contentResolver.openInputStream(Uri.parse(sourceFile.path)),
//          targetPath,
//          StandardCopyOption.REPLACE_EXISTING
//        )
//      } else {
//        Files.copy(Paths.get(sourceFile.path), targetPath, StandardCopyOption.REPLACE_EXISTING)
//      }
//      newFile.path = targetPath.toString()
//    }
//    return newFile
//  }
//
//  fun moveFile(sourceFile: Media, destinationUri: Uri) {
//    getPath(destinationUri)?.let { path ->
//      val targetPath = path.resolve(sourceFile.name)
//      Files.move(Paths.get(sourceFile.path), targetPath, StandardCopyOption.REPLACE_EXISTING)
//      sourceFile.path = targetPath.toString()
//    }
//  }

//  fun rename(fileEntity: FileEntity, newName: String) {
//    val newPath = Paths.get(fileEntity.path).parent.resolve(newName)
//    if (!File(fileEntity.path).renameTo(newPath.toFile())) {
//      throw Exception("Rename failed")
//    }
//    fileEntity.name = newName
//    fileEntity.path = newPath.toString()
//  }

  fun delete(file: FileEntity) {
    if (file.isUri) {
      AndroidDocFile.delete(DocumentFile.fromTreeUri(MainActivity.context, Uri.parse(file.path))!!)
    } else {
      AndroidPath.delete(Paths.get(file.path))
    }
  }
}
