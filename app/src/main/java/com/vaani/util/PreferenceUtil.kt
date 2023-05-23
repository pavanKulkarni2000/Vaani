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

    fun save() {
        val preference = MainActivity.context.getSharedPreferences(MainActivity.context.getString(R.string.app_key), Context.MODE_PRIVATE)
        with(preference.edit()) {
            putLong(FOLDER_LAST_PLAYED_KEY, favouriteFolder.lastPlayedId)
            putLong(FOLDER_LAST_PLAYED_KEY, lastPlayedFolderId)
            apply()
        }
    }


    fun collectionLastPlayed(folderEntity: FolderEntity) {
//        var file: FileEntity? = null
//        if (Player.state.isPlaying.value!! && Player.state.file.value?.folderId == folderEntity.id) {
//            file = Player.state.file.value
//        } else {
//            DB.getCollectionPreference(folderEntity.id)?.let { collPref ->
//                file = folderMediaViewModel.folderMediaList.value?.find { it.id == collPref.lastPlayedId }
//            }
//        }
//        file?.let {
//            Player.startNewMedia(it)
//            parentFragmentManager.commit {
//                add(R.id.fragment_container_view, VlcPlayerFragment::class.java, null, TAG)
//                addToBackStack(null)
//            }
//        }
    }

    fun playLastCollectionLastPlayed() {
//        var file: FileEntity? = null
//        var folderEntity: FolderEntity? = null
//        if (Player.state.isPlaying.value!! && Player.state.file.value?.folderId != Constants.FAVOURITE_COLLECTION_ID) {
//            file = Player.state.file.value
//            file.let { file1 ->
//                folderEntity = foldersViewModel.folderList.value?.find { it.id == file1.folderId }
//            }
//        } else {
//            folderEntity = foldersViewModel.folderList.value?.find { it.id == Folders.lastPlayedFolderId }
//            folderEntity?.let { folder1 ->
//                val folderFiles = DB.getFolderMediaList(folder1.id)
//                DB.getCollectionPreference(folder1.id)?.let { collectionPref ->
//                    file = folderFiles.find { it.id == collectionPref.lastPlayedId }
//                } ?: run {
//                    file = folderFiles[0]
//                }
//            }
//        }
//        requireParentFragment().parentFragmentManager.commit {
//            folderEntity?.let {
//                add(R.id.fragment_container_view, FolderMediaListFragment(it))
//            }
//            file?.let {
//                Player.startNewMedia(it)
//                add(R.id.fragment_container_view, VlcPlayerFragment::class.java, null, TAG)
//            }
//            addToBackStack(null)
//        }
//        collectionLastPlayed()
    }

    fun close() {
        save()
    }
}