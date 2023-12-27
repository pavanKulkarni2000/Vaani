package com.vaani.util

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.UriPermission
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.vaani.MainActivity
import com.vaani.data.util.FileUtil

object PermissionUtil {

  fun managePermissions(context: Context) {

    if (!checkAllFileAccess()) {
      requestAllFilesPermission(context.packageName)
    }
    if (!checkOtherFilePermissions(context)) {
      requestOtherFilePermissions()
    }
    if (!checkAndroidFolderAccess("data", context.contentResolver)) {
      requestAndroidFolderPermission("data")
    }
    if (!checkAndroidFolderAccess("obb", context.contentResolver)) {
      requestAndroidFolderPermission("obb")
    }
  }

  private fun checkAllFileAccess(): Boolean {
    return Environment.isExternalStorageManager()
  }

  private fun checkOtherFilePermissions(context: Context): Boolean {
    return context.checkSelfPermission(READ_EXTERNAL_STORAGE) ==
      PackageManager.PERMISSION_GRANTED &&
      context.checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
  }

  private fun checkAndroidFolderAccess(folder: String, contentResolver: ContentResolver): Boolean {
    val uri = Uri.parse(FileUtil.androidFolderTreeUriStr(folder))
    return contentResolver.persistedUriPermissions.any { element: UriPermission ->
      element.uri == uri && element.isReadPermission && element.isWritePermission
    }
  }

  private fun requestAllFilesPermission(packageName: String) {
    val activityWithResultLauncher =
      MainActivity.fragmentActivity.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
      ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
          val data: Intent? = result.data
          if (data != null) {
            data.data?.let { treeUri ->
              MainActivity.contentResolver.takePersistableUriPermission(
                treeUri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
              )
            }
          }
        }
      }
    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
    intent.addCategory("android.intent.category.DEFAULT")
    intent.data = Uri.fromParts("package", packageName, null)
    activityWithResultLauncher.launch(intent)
  }

  private fun requestAndroidFolderPermission(folder: String) {
    val docTreeLauncher =
      MainActivity.fragmentActivity.registerForActivityResult(
        ActivityResultContracts.OpenDocumentTree()
      ) { treeUri ->
        if (treeUri != null) {
          MainActivity.contentResolver.takePersistableUriPermission(
            treeUri,
            Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
          )
        }
      }
    val uri = Uri.parse(FileUtil.androidFolderTreeUriStr(folder).replace("tree", "document"))
    docTreeLauncher.launch(uri)
  }

  private fun requestOtherFilePermissions() {
    val requestPermissionLauncher =
      MainActivity.fragmentActivity.registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
      ) {
        it.forEach { entry: Map.Entry<String, Boolean> ->
          if (!entry.value) {
            Log.d(TAG, "${entry.key}: not allowed")
            MainActivity.fragmentActivity.finish()
          }
        }
      }
    requestPermissionLauncher.launch(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE))
  }
}
