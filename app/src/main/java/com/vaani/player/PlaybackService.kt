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

import android.app.PendingIntent
import android.app.PendingIntent.*
import android.app.TaskStackBuilder
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_CHANGE_MEDIA_ITEMS
import androidx.media3.common.Player.COMMAND_SEEK_TO_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS
import androidx.media3.common.Player.COMMAND_STOP
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_SEEK
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ControllerInfo
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.vaani.data.Files
import com.vaani.data.PlayerData
import com.vaani.player.PlayerUtil.closeButton
import com.vaani.player.PlayerUtil.closeCommand
import com.vaani.ui.MainActivity
import com.vaani.ui.player.PlayerActivity
import com.vaani.util.TAG

@UnstableApi
class PlaybackService : MediaSessionService() {
  private lateinit var player: ExoPlayer
  private lateinit var mediaSession: MediaSession

  @OptIn(UnstableApi::class)
  override fun onCreate() {
    super.onCreate()
    player =
      ExoPlayer.Builder(this)
        .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus = */ true)
        .build()
    player.addListener(MyPlayerListener())
    mediaSession =
      MediaSession.Builder(this, player)
        .setCallback(CustomMediaSessionCallback())
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

  private inner class CustomMediaSessionCallback : MediaSession.Callback {

    override fun onConnect(
      session: MediaSession,
      controller: ControllerInfo
    ): MediaSession.ConnectionResult {
      val connectionResult = super.onConnect(session, controller)
      val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
      availableSessionCommands.add(closeCommand)
      return MediaSession.ConnectionResult.accept(
        availableSessionCommands.build(),
        connectionResult.availablePlayerCommands
      )
    }

    override fun onCustomCommand(
      session: MediaSession,
      controller: ControllerInfo,
      customCommand: SessionCommand,
      args: Bundle
    ): ListenableFuture<SessionResult> {
      if (customCommand == closeCommand) {
        PlayerUtil.saveProgress(
          session.player.currentMediaItemIndex,
          session.player.currentPosition
        )
        session.player.stop()
      }
      return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }

    override fun onPostConnect(session: MediaSession, controller: ControllerInfo) {
      session.setCustomLayout(controller, mutableListOf(closeButton))
      super.onPostConnect(session, controller)
    }

    override fun onAddMediaItems(
      mediaSession: MediaSession,
      controller: ControllerInfo,
      mediaItems: MutableList<MediaItem>
    ): ListenableFuture<MutableList<MediaItem>> {
      val updatedMediaItems = mediaItems.map { MediaItem.fromUri(it.mediaId) }.toMutableList()
      return Futures.immediateFuture(updatedMediaItems)
    }

    override fun onPlayerCommandRequest(
      session: MediaSession,
      controller: ControllerInfo,
      playerCommand: Int
    ): Int {
      Log.d(TAG, "onPlayerCommandRequest: command received $playerCommand")
      when (playerCommand) {
        COMMAND_SEEK_TO_NEXT,
        COMMAND_SEEK_TO_PREVIOUS,
        COMMAND_SEEK_TO_MEDIA_ITEM,
        COMMAND_CHANGE_MEDIA_ITEMS,
        COMMAND_STOP ->
          PlayerUtil.saveProgress(
            session.player.currentMediaItemIndex,
            session.player.currentPosition
          )
      }
      return super.onPlayerCommandRequest(session, controller, playerCommand)
    }
  }

  private inner class MyPlayerListener : Player.Listener {

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
      super.onMediaItemTransition(mediaItem, reason)
      when (reason) {
        MEDIA_ITEM_TRANSITION_REASON_AUTO,
        MEDIA_ITEM_TRANSITION_REASON_SEEK,
        MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> {
          if (reason == MEDIA_ITEM_TRANSITION_REASON_AUTO) {
            PlayerUtil.saveProgress(player.previousMediaItemIndex, 0)
          }
          Files.updateLastPlayedItems(
            PlayerData.currentCollection,
            PlayerData.currentPlayList[player.currentMediaItemIndex].id
          )
          player.seekTo(
            PlayerUtil.getMediaProgressMs(PlayerData.currentPlayList[player.currentMediaItemIndex])
          )
        }
        MEDIA_ITEM_TRANSITION_REASON_REPEAT -> {}
      }
    }
  }

  private fun appIntent(): PendingIntent {
    return TaskStackBuilder.create(this@PlaybackService).run {
      addNextIntent(Intent(this@PlaybackService, MainActivity::class.java))
      addNextIntent(
        Intent(this@PlaybackService, PlayerActivity::class.java)
          .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
      )
      getPendingIntent(0, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
    }
  }
}
