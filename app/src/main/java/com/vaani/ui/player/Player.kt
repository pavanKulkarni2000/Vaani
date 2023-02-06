package com.vaani.ui.player

import android.app.Activity
import android.content.Intent

object Player {
    lateinit var mediaPlayerService: PlayerServiceListener
    fun init(activity: Activity) {
        val intent = Intent(activity.applicationContext, PlayerService::class.java)
        activity.startService(intent)
    }
}