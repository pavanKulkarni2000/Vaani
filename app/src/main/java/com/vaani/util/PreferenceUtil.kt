package com.vaani.util

import android.content.Context
import com.vaani.MainActivity
import com.vaani.R
import com.vaani.models.FolderEntity
import com.vaani.util.Constants.FAVOURITE_LAST_PLAYED_KEY
import com.vaani.util.Constants.FOLDER_LAST_PLAYED_KEY

object PreferenceUtil {

    val favouriteFolder = FolderEntity()
    var lastPlayedFolderId: Long = -1

    fun init(context: Context) {
        val preference = context.getSharedPreferences(context.getString(R.string.app_key), Context.MODE_PRIVATE)
        favouriteFolder.lastPlayedId = preference.getLong(FAVOURITE_LAST_PLAYED_KEY, 0)
        lastPlayedFolderId = preference.getLong(FOLDER_LAST_PLAYED_KEY, 0)
    }

    fun close() {
        val preference = MainActivity.context.getSharedPreferences(MainActivity.context.getString(R.string.app_key), Context.MODE_PRIVATE)
        with(preference.edit()) {
            putLong(FOLDER_LAST_PLAYED_KEY, favouriteFolder.lastPlayedId)
            putLong(FOLDER_LAST_PLAYED_KEY, lastPlayedFolderId)
            apply()
        }
    }
}