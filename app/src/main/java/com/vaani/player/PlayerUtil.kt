package com.vaani.player

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.vaani.util.TAG
import kotlin.math.log

@UnstableApi
object PlayerUtil {

    private lateinit var controllerFuture : ListenableFuture<MediaController>
     val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    fun init(context: Context){
        controllerFuture = MediaController.Builder(
                context,
        SessionToken(context, ComponentName(context, PlaybackService::class.java))
        ).buildAsync()
        controllerFuture.addListener({
                                     controller?.run {
                                         addListener(object: Player.Listener{
                                             override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                                                 super.onMediaItemTransition(mediaItem, reason)
                                                 Log.d(TAG, "onMediaItemTransition: trans")
                                             }
                                             override fun onEvents(player: Player, events: Player.Events) {
                                                 super.onEvents(player, events)
                                                 for(index in 0 until events.size()){
                                                     when (events.get(index)) {
                                                         Player.EVENT_IS_PLAYING_CHANGED -> Log.d(
                                                             TAG,
                                                             "onEvents: isPlaying changed for ${player.currentMediaItem?.mediaId} progress: ${player.currentPosition} at ${player.currentMediaItemIndex}"
                                                         )
                                                         Player.EVENT_REPEAT_MODE_CHANGED -> Log.d(TAG, "onEvents: repeat changed for ${player.currentMediaItem?.mediaId} at ${player.currentMediaItemIndex}")
                                                         Player.EVENT_MEDIA_ITEM_TRANSITION -> Log.d(TAG, "onEvents: media transition for ${player.currentMediaItem?.mediaId} at ${player.currentMediaItemIndex}")
                                                         Player.EVENT_SHUFFLE_MODE_ENABLED_CHANGED -> Log.d(TAG, "onEvents: shuffle changed for ${player.currentMediaItem?.mediaId} at ${player.currentMediaItemIndex}")
                                                         Player.EVENT_PLAYBACK_STATE_CHANGED -> Log.d(TAG, "onEvents: playback state changed to ${player.playbackState} for ${player.currentMediaItem!!.mediaId} at ${player.currentMediaItemIndex}")
                                                         Player.EVENT_PLAYER_ERROR -> Log.e(TAG, "onEvents: Error : ",player.playerError)
                                                         Player.EVENT_TRACKS_CHANGED -> Log.d(TAG, "onEvents: tracks changed at ${player.currentMediaItemIndex} prev ${player.previousMediaItemIndex}")
                                                         else -> {}
                                                     }
                                                 }
                                             }
                                         })
                                     }
        }, MoreExecutors.newDirectExecutorService())
    }

    fun close(){
        MediaController.releaseFuture(controllerFuture)
    }
}