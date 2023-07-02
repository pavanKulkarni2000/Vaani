/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.vaani.player

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.*
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ControllerInfo
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.vaani.MainActivity
import com.vaani.R
import com.vaani.util.AppAction
import com.vaani.util.Constants.ACTION
import com.vaani.util.TAG
import androidx.media3.ui.R as media3R


@UnstableApi
class PlaybackService : MediaSessionService() {
    private lateinit var sessionCallback: CustomMediaSessionCallback
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        sessionCallback = CustomMediaSessionCallback()
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus = */ true)
            .build()
        mediaSession = MediaSession.Builder(this, player)
            .setCallback(sessionCallback)
            .setSessionActivity(appIntent())
            .build()
    }

    override fun onGetSession(controllerInfo: ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.playWhenReady) {
            stopSelf()
        }
    }

    override fun onDestroy() {
        player.release()
        mediaSession.release()
        clearListener()
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 123
        private const val CHANNEL_ID = "demo_session_notification_channel_id"
    }


    private inner class CustomMediaSessionCallback : MediaSession.Callback {

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            val updatedMediaItems = mediaItems.map { MediaItem.fromUri(it.mediaId) }.toMutableList()
            return Futures.immediateFuture(updatedMediaItems)
        }
    }

    private fun appIntent(): PendingIntent {
        return TaskStackBuilder.create(this@PlaybackService).run {
            addNextIntent(Intent(this@PlaybackService, MainActivity::class.java).apply {
                putExtra(
                    ACTION,
                    AppAction.ACTION_START_PLAYER
                )
            })
            getPendingIntent(0, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
        }
    }

}
