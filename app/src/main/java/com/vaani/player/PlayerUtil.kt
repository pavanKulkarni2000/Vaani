package com.vaani.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.vaani.ui.MainActivity
import com.vaani.R
import com.vaani.data.Files
import com.vaani.data.PlayerData
import com.vaani.data.model.Media
import com.vaani.ui.player.PlayerActivity
import com.vaani.util.TAG

@UnstableApi
object PlayerUtil {

  private lateinit var controllerFuture: ListenableFuture<MediaController>
  val controller: MediaController?
    get() = if (controllerFuture.isDone) controllerFuture.get() else null

  private const val actionClose = "CLOSE"
  val closeCommand = SessionCommand(actionClose, Bundle.EMPTY)
  val closeButton =
    CommandButton.Builder()
      .setSessionCommand(closeCommand)
      .setIconResId(R.drawable.mediacontroller_close_40px)
      .setDisplayName("Close")
      .setEnabled(true)
      .build()

  fun init(context: Context) {
    controllerFuture =
      MediaController.Builder(
          context,
          SessionToken(context, ComponentName(context, PlaybackService::class.java))
        )
        .buildAsync()
  }

  fun getMediaProgressMs(file: Media): Long =
    (file.duration * file.playBackProgress * 1000).toLong()

  private fun getMediaProgress(file: Media, position: Long): Float =
    (position.toFloat() / (file.duration * 1000))

  fun play(playList: List<Media>, position: Int, collectionId: Long) {
    val controller = this.controller ?: return
    val file = playList[position]
    if (PlayerData.currentCollection == collectionId && PlayerData.currentPlayList == playList) {
      controller.seekTo(position, getMediaProgressMs(file))
    } else {
      controller.setMediaItems(
        playList.map { MediaItem.Builder().setMediaId(it.path).build() },
        position,
        getMediaProgressMs(file)
      )
      PlayerData.setCurrent(collectionId, playList)
    }
//    controller.setPlaybackSpeed(file.playBackSpeed)
//    controller.repeatMode = if (file.playBackLoop) REPEAT_MODE_ONE else REPEAT_MODE_OFF
    //        controller.shuffleModeEnabled = Files.getFolder(file.folderId).playBackShuffle
    controller.prepare()
    controller.play()
    MainActivity.context.startActivity(
      Intent(MainActivity.context, PlayerActivity::class.java)
        .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
    )
  }

  fun startPlayerActivity() {

    if (controller?.isPlaying == true) {
      MainActivity.context.startActivity(
        Intent(MainActivity.context, PlayerActivity::class.java)
          .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
      )
    }
  }

  fun close() {
    MediaController.releaseFuture(controllerFuture)
  }

  fun resume(context: Context) {
    if (controller?.isConnected != true) {
      init(context)
    }
  }

  fun saveProgress(mediaIndex: Int, position: Long) {
    try {
      val endMedia = PlayerData.currentPlayList[mediaIndex]
      endMedia.playBackProgress = getMediaProgress(endMedia, position)
      Files.saveProgress(endMedia)
    } catch (e: Exception) {
      Log.e(TAG, "saveProgress: error ", e)
    }
  }
}
