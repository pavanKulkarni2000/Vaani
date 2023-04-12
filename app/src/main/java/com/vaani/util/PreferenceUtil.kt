package com.vaani.util

import android.content.Context
import com.vaani.R
import com.vaani.models.CollectionPreference
import com.vaani.models.FavSortOrder
import com.vaani.util.Constants.FAVOURITE_LAST_PLAYED_KEY
import com.vaani.util.Constants.FAVOURITE_SORT_ORDER_KEY
import com.vaani.util.Constants.FOLDER_LAST_PLAYED_KEY

object PreferenceUtil {
    object Favourite {
        fun put(collectionPreference: CollectionPreference) {
            lastPlayedId = collectionPreference.lastPlayedId
        }

        var lastPlayedId: Long = 0
        var sort: FavSortOrder = FavSortOrder.RANK
    }

    object Folders {

        var lastPlayedFolderId: Long = 0
    }


    fun init(context: Context) {
        val preference = context.getSharedPreferences(context.getString(R.string.app_key), Context.MODE_PRIVATE)
        Favourite.sort = FavSortOrder.valueOf(preference.getString(FAVOURITE_SORT_ORDER_KEY, "RANK") ?: "RANK")
        Favourite.lastPlayedId = preference.getLong(FAVOURITE_LAST_PLAYED_KEY, 0)
        Folders.lastPlayedFolderId = preference.getLong(FOLDER_LAST_PLAYED_KEY, 0)
    }

    val favPreference
        get() = CollectionPreference(-1, -1, shuffle = false, lastPlayedId = Favourite.lastPlayedId)

    fun close(context: Context) {
        val preference = context.getSharedPreferences(context.getString(R.string.app_key), Context.MODE_PRIVATE)
        with(preference.edit()) {
            putLong(FOLDER_LAST_PLAYED_KEY, Favourite.lastPlayedId)
            putString(FAVOURITE_SORT_ORDER_KEY, Favourite.sort.name)
            putLong(FOLDER_LAST_PLAYED_KEY, Folders.lastPlayedFolderId)
            apply()
        }
    }
}