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

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
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
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ControllerInfo
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.vaani.MainActivity
import com.vaani.R
import com.vaani.ui.player.PlayerFragment
import com.vaani.util.TAG
import androidx.media3.ui.R as media3R


@UnstableApi
class PlaybackService : MediaLibraryService() {
  private val librarySessionCallback = CustomMediaLibrarySessionCallback()
  private lateinit var player: ExoPlayer
  private lateinit var mediaLibrarySession: MediaLibrarySession

  @OptIn(UnstableApi::class)
  override fun onCreate() {
    super.onCreate()
    player = ExoPlayer.Builder(this)
      .setAudioAttributes(AudioAttributes.DEFAULT, /* handleAudioFocus = */ true).build()
    mediaLibrarySession = MediaLibrarySession.Builder(this, player,librarySessionCallback)
      .setSessionActivity(TaskStackBuilder.create(this).run {
        addNextIntent(Intent(this@PlaybackService, MainActivity::class.java))
        getPendingIntent(0, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
      }
    )
      .build()
    setListener(MediaSessionServiceListener())
  }

  override fun onGetSession(controllerInfo: ControllerInfo): MediaLibrarySession {
    return mediaLibrarySession
  }

  override fun onTaskRemoved(rootIntent: Intent?) {
    if (!player.playWhenReady) {
      stopSelf()
    }
  }

  override fun onDestroy() {
    player.release()
    mediaLibrarySession.release()
    clearListener()
    super.onDestroy()
  }

  companion object {
    private const val NOTIFICATION_ID = 123
    private const val CHANNEL_ID = "demo_session_notification_channel_id"
  }


  private inner class MediaSessionServiceListener : Listener {
    override fun onForegroundServiceStartNotAllowedException() {
      val notificationManagerCompat = NotificationManagerCompat.from(this@PlaybackService)
      ensureNotificationChannel(notificationManagerCompat)
      val pendingIntent =
        TaskStackBuilder.create(this@PlaybackService).run {
          addNextIntent(Intent(this@PlaybackService, MainActivity::class.java))
          getPendingIntent(0, FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT)
        }
      val builder =
        NotificationCompat.Builder(this@PlaybackService, CHANNEL_ID)
          .setContentIntent(pendingIntent)
          .setSmallIcon(R.drawable.app_ic_launcher_foreground)
          .setContentTitle(getString(R.string.notification_content_title))
          .setStyle(
            NotificationCompat.BigTextStyle().bigText(getString(R.string.notification_content_text))
          )
          .setPriority(NotificationCompat.PRIORITY_DEFAULT)
          .setAutoCancel(true)
      notificationManagerCompat.notify(NOTIFICATION_ID, builder.build())
    }
  }

  private fun  ensureNotificationChannel(notificationManagerCompat: NotificationManagerCompat) {
    if (Util.SDK_INT < 26 || notificationManagerCompat.getNotificationChannel(CHANNEL_ID) != null) {
      return
    }

    val channel =
      NotificationChannel(
        CHANNEL_ID,
        getString(R.string.notification_channel_name),
        NotificationManager.IMPORTANCE_DEFAULT
      )
    notificationManagerCompat.createNotificationChannel(channel)
  }


  private val shuffleOnCommand = SessionCommand("SHUFFLE_ON", Bundle.EMPTY)
  private val shuffleOnButton = CommandButton.Builder()
    .setDisplayName(getString(R.string.controls_shuffle_on_description))
    .setSessionCommand(shuffleOnCommand)
    .setIconResId( media3R.drawable.exo_icon_shuffle_off )
    .build()

  private val shuffleOffCommand = SessionCommand("SHUFFLE_ON", Bundle.EMPTY)
  private val shuffleOffButton = CommandButton.Builder()
    .setDisplayName(getString(R.string.controls_shuffle_off_description))
    .setSessionCommand(shuffleOffCommand)
    .setIconResId(media3R.drawable.exo_icon_shuffle_on)
    .build()
  private inner class CustomMediaLibrarySessionCallback : MediaLibrarySession.Callback {

    override fun onConnect(
      session: MediaSession,
      controller: ControllerInfo
    ): MediaSession.ConnectionResult {
      val connectionResult = super.onConnect(session, controller)
      val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
      availableSessionCommands.add(shuffleOnCommand)
      availableSessionCommands.add(shuffleOffCommand)
      return MediaSession.ConnectionResult.accept(
        availableSessionCommands.build(),
        connectionResult.availablePlayerCommands
      )
    }

    override fun onPostConnect(session: MediaSession, controller: ControllerInfo) {
      Log.d(TAG, "onPostConnect: custom layout")
//      if (!customLayout.isEmpty() && controller.controllerVersion != 0) {
//        // Let Media3 controller (for instance the MediaNotificationProvider) know about the custom
//        // layout right after it connected.
//        ignoreFuture(mediaLibrarySession.setCustomLayout(controller, customLayout))
//      }
    }

    override fun onCustomCommand(
      session: MediaSession,
      controller: ControllerInfo,
      customCommand: SessionCommand,
      args: Bundle
    ): ListenableFuture<SessionResult> {
      if (shuffleOnCommand.customAction == customCommand.customAction) {
        // Enable shuffling.
        player.shuffleModeEnabled = true
        // Change the custom layout to contain the `Disable shuffling` command.
//        customLayout = ImmutableList.of(customCommands[1])
        // Send the updated custom layout to controllers.
        session.setCustomLayout(listOf(shuffleOffButton))
      } else if (shuffleOffCommand.customAction == customCommand.customAction) {
        // Disable shuffling.
        player.shuffleModeEnabled = false
        // Change the custom layout to contain the `Enable shuffling` command.
//        customLayout = ImmutableList.of(customCommands[0])
        // Send the updated custom layout to controllers.
        session.setCustomLayout(listOf(shuffleOnButton))
      }
      return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
    }

    override fun onGetLibraryRoot(
      session: MediaLibrarySession,
      browser: ControllerInfo,
      params: LibraryParams?
    ): ListenableFuture<LibraryResult<MediaItem>> {
      if (params != null && params.isRecent) {
        // The service currently does not support playback resumption. Tell System UI by returning
        // an error of type 'RESULT_ERROR_NOT_SUPPORTED' for a `params.isRecent` request. See
        // https://github.com/androidx/media/issues/355
        return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_NOT_SUPPORTED))
      }
      return Futures.immediateFuture(LibraryResult.ofItem(MediaProvider.root, params))
    }

    override fun onGetItem(
      session: MediaLibrarySession,
      browser: ControllerInfo,
      mediaId: String
    ): ListenableFuture<LibraryResult<MediaItem>> {
      val item =
        MediaItemTree.getItem(mediaId)
          ?: return Futures.immediateFuture(
            LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
          )
      return Futures.immediateFuture(LibraryResult.ofItem(item, /* params= */ null))
    }

    override fun onSubscribe(
      session: MediaLibrarySession,
      browser: ControllerInfo,
      parentId: String,
      params: LibraryParams?
    ): ListenableFuture<LibraryResult<Void>> {
      val children =
        MediaItemTree.getChildren(parentId)
          ?: return Futures.immediateFuture(
            LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
          )
      session.notifyChildrenChanged(browser, parentId, children.size, params)
      return Futures.immediateFuture(LibraryResult.ofVoid())
    }

    override fun onGetChildren(
      session: MediaLibrarySession,
      browser: ControllerInfo,
      parentId: String,
      page: Int,
      pageSize: Int,
      params: LibraryParams?
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
      val children =
        MediaItemTree.getChildren(parentId)
          ?: return Futures.immediateFuture(
            LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
          )

      return Futures.immediateFuture(LibraryResult.ofItemList(children, params))
    }

    override fun onAddMediaItems(
      mediaSession: MediaSession,
      controller: MediaSession.ControllerInfo,
      mediaItems: List<MediaItem>
    ): ListenableFuture<List<MediaItem>> {
      val updatedMediaItems: List<MediaItem> =
        mediaItems.map { mediaItem ->
          if (mediaItem.requestMetadata.searchQuery != null)
            getMediaItemFromSearchQuery(mediaItem.requestMetadata.searchQuery!!)
          else MediaItemTree.getItem(mediaItem.mediaId) ?: mediaItem
        }
      return Futures.immediateFuture(updatedMediaItems)
    }

    private fun getMediaItemFromSearchQuery(query: String): MediaItem {
      // Only accept query with pattern "play [Title]" or "[Title]"
      // Where [Title]: must be exactly matched
      // If no media with exact name found, play a random media instead
      val mediaTitle =
        if (query.startsWith("play ", ignoreCase = true)) {
          query.drop(5)
        } else {
          query
        }

      return MediaItemTree.getItemFromTitle(mediaTitle) ?: MediaItemTree.getRandomItem()
    }
  }

}
