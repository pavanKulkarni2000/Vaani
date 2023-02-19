package com.vaani.ui.player

import android.app.Activity
import android.content.Intent
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vaani.models.File
import com.vaani.ui.folderList.FoldersViewModel

object Player {
    lateinit var mediaPlayerService: PlayerServiceListener
     val state = PlayerState()
    fun init(activity: Activity) {
        val intent = Intent(activity.applicationContext, PlayerService::class.java)
        activity.startService(intent)
    }

    fun startNewMedia(file: File) {
        state.setCurrentFile(file)
    }
}