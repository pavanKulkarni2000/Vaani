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
import androidx.activity.result.ActivityResultLauncher
import com.vaani.MainActivity

object PermissionUtil {

    fun managePermissions(mainActivity: MainActivity) {
        if (!checkAllFileAccess()) {
            requestAllFilesPermission(mainActivity.packageName, mainActivity.activityWithResultLauncher)
        }
        if (!checkOtherFilePermissions(mainActivity.applicationContext)) {
            requestOtherFilePermissions(mainActivity.requestPermissionLauncher)
        }
        if (!checkAndroidFolderAccess("data", mainActivity.contentResolver)) {
            requestAndroidFolderPermission("data", mainActivity.docTreeLauncher)
        }
        if (!checkAndroidFolderAccess("obb", mainActivity.contentResolver)) {
            requestAndroidFolderPermission("obb", mainActivity.docTreeLauncher)
        }
    }

    private fun checkAllFileAccess(): Boolean {
        return Environment.isExternalStorageManager()
    }

    private fun checkOtherFilePermissions(context: Context): Boolean {
        return context.checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkAndroidFolderAccess(folder: String, contentResolver: ContentResolver): Boolean {
        val uri = Uri.parse(FileUtil.androidFolderTreeUriStr(folder))
        return contentResolver.persistedUriPermissions.any { element: UriPermission ->
            element.uri == uri &&
                    element.isReadPermission &&
                    element.isWritePermission
        }
    }

    private fun requestAllFilesPermission(
        packageName: String,
        activityWithResultLauncher: ActivityResultLauncher<Intent>
    ) {
        val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
        intent.addCategory("android.intent.category.DEFAULT")
        intent.data = Uri.fromParts("package", packageName, null)
        activityWithResultLauncher.launch(intent)
    }

    private fun requestAndroidFolderPermission(
        folder: String,
        docTreeLauncher: ActivityResultLauncher<Uri?>,
    ) {
        val uri = Uri.parse(FileUtil.androidFolderTreeUriStr(folder).replace("tree", "document"))
        docTreeLauncher.launch(uri)

    }

    private fun requestOtherFilePermissions(requestPermissionLauncher: ActivityResultLauncher<Array<String>>) {
        requestPermissionLauncher.launch(arrayOf(READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE))
    }

}