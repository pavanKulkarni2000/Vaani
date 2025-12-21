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
import androidx.media3.common.Player.*
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.ControllerInfo
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.vaani.MainActivity
import com.vaani.VaaniApplication
import com.vaani.dal.MediaManager
import com.vaani.ui.player.PlayerActivity
import com.vaani.util.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@UnstableApi
class PlaybackService : MediaSessionService() {
    private lateinit var player: ExoPlayer
    private lateinit var mediaSession: MediaSession
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    private lateinit var mediaManager: MediaManager
    private lateinit var playerUtil: PlayerUtil

    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val container = (application as VaaniApplication).container
        mediaManager = container.mediaManager
        playerUtil = container.playerUtil
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
        serviceJob.cancel()
        super.onDestroy()
    }

    private inner class CustomMediaSessionCallback : MediaSession.Callback {

        override fun onConnect(
            session: MediaSession,
            controller: ControllerInfo,
        ): MediaSession.ConnectionResult {
            val connectionResult = super.onConnect(session, controller)
            val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
            availableSessionCommands.add(playerUtil.closeCommand)
            return MediaSession.ConnectionResult.accept(
                availableSessionCommands.build(),
                connectionResult.availablePlayerCommands,
            )
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle,
        ): ListenableFuture<SessionResult> {
            if (customCommand == playerUtil.closeCommand) {
                serviceScope.launch {
                    playerUtil.saveProgress(
                        session.player.currentMediaItemIndex,
                        session.player.currentPosition,
                    )
                }
                session.player.stop()
            }
            return Futures.immediateFuture(SessionResult(SessionResult.RESULT_SUCCESS))
        }

        override fun onPostConnect(session: MediaSession, controller: ControllerInfo) {
            session.setCustomLayout(controller, mutableListOf(playerUtil.closeButton))
            super.onPostConnect(session, controller)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: ControllerInfo,
            mediaItems: MutableList<MediaItem>,
        ): ListenableFuture<MutableList<MediaItem>> {
            val updatedMediaItems = mediaItems.map { MediaItem.fromUri(it.mediaId) }.toMutableList()
            return Futures.immediateFuture(updatedMediaItems)
        }

        override fun onPlayerCommandRequest(
            session: MediaSession,
            controller: ControllerInfo,
            playerCommand: Int,
        ): Int {
            serviceScope.launch {
                when (playerCommand) {
                    COMMAND_SEEK_TO_NEXT,
                    COMMAND_SEEK_TO_PREVIOUS,
                    COMMAND_SEEK_TO_MEDIA_ITEM,
                    COMMAND_CHANGE_MEDIA_ITEMS,
                    COMMAND_STOP ->
                        playerUtil.saveProgress(
                            session.player.currentMediaItemIndex,
                            session.player.currentPosition,
                        )
                }
            }
            return super.onPlayerCommandRequest(session, controller, playerCommand)
        }
    }

    private inner class MyPlayerListener : Player.Listener {

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            serviceScope.launch {
                when (reason) {
                    MEDIA_ITEM_TRANSITION_REASON_AUTO,
                    MEDIA_ITEM_TRANSITION_REASON_SEEK,
                    MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED -> {
                        if (reason == MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                            playerUtil.saveProgress(player.previousMediaItemIndex, 0)
                        }
                        if (PlayerData.currentPlayList.isNotEmpty()) {
                            mediaManager.updateLastPlayedItems(
                                PlayerData.currentCollection,
                                PlayerData.currentPlayList[player.currentMediaItemIndex].id,
                            )
                            player.seekTo(
                                playerUtil.getMediaProgressMs(PlayerData.currentPlayList[player.currentMediaItemIndex])
                            )
                        }
                    }
                    MEDIA_ITEM_TRANSITION_REASON_REPEAT -> {}
                }
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
