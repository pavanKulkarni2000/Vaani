package com.vaani

import android.content.Context
import com.vaani.dal.FavoritesRepository
import com.vaani.dal.FileManager
import com.vaani.dal.FolderRepository
import com.vaani.dal.MediaManager
import com.vaani.dal.MediaRepository
import com.vaani.db.AppDatabase
import com.vaani.player.PlayerUtil

/**
 * A container for dependencies that are shared across the app.
 */
class AppContainer(context: Context) {

    private val database by lazy { AppDatabase.getDatabase(context) }

    val folderRepository by lazy {
        FolderRepository(database.folderDao())
    }

    val mediaRepository by lazy {
        MediaRepository(database.mediaDao(), database.folderDao(), database)
    }

    val favoritesRepository by lazy {
        FavoritesRepository(database.favouriteDao())
    }

    val fileManager by lazy {
        FileManager(context, folderRepository, mediaRepository, favoritesRepository)
    }

    val mediaManager by lazy {
        MediaManager(folderRepository, mediaRepository)
    }

    val playerUtil by lazy {
        PlayerUtil(context, mediaManager, folderRepository)
    }
}
