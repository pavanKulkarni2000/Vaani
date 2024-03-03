package com.vaani.util

import android.content.Context
import com.vaani.ui.MainActivity
import com.vaani.R
import com.vaani.data.Files
import com.vaani.util.Constants.FAVOURITE_LAST_PLAYED_KEY
import com.vaani.util.Constants.FOLDER_LAST_PLAYED_KEY

object PreferenceUtil {
  var lastPlayedFolderId: Long = -1

  fun init(context: Context) {
    val preference =
      context.getSharedPreferences(context.getString(R.string.app_key), Context.MODE_PRIVATE)
    Files.favouriteFolder.lastPlayedId = preference.getLong(FAVOURITE_LAST_PLAYED_KEY, 0)
    lastPlayedFolderId = preference.getLong(FOLDER_LAST_PLAYED_KEY, 0)
  }

  fun save() {
    val preference =
      MainActivity.context.getSharedPreferences(
        MainActivity.context.getString(R.string.app_key),
        Context.MODE_PRIVATE
      )
    with(preference.edit()) {
      putLong(FOLDER_LAST_PLAYED_KEY, Files.favouriteFolder.lastPlayedId)
      putLong(FOLDER_LAST_PLAYED_KEY, lastPlayedFolderId)
      apply()
    }
  }

  fun close() = save()
}
