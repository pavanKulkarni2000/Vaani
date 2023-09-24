package com.vaani.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.MediaController
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.vaani.MainActivity
import com.vaani.R
import com.vaani.data.Files
import com.vaani.data.PlayerData
import com.vaani.models.FolderEntity
import com.vaani.models.MediaEntity
import com.vaani.ui.player.PlayerActivity
import com.vaani.util.TAG

@UnstableApi
object PlayerUtil {

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null


    val actionClose = "CLOSE"
    val closeCommand = SessionCommand(actionClose, Bundle.EMPTY)
    val closeButton = CommandButton.Builder()
        .setSessionCommand(closeCommand)
        .setIconResId(R.drawable.mediacontroller_close_40px)
        .setDisplayName("Close")
        .setEnabled(true)
        .build()

    fun init(context: Context) {
        controllerFuture = MediaController.Builder(
            context,
            SessionToken(context, ComponentName(context, PlaybackService::class.java))
        ).buildAsync()
    }

    fun getMediaProgressMs(file: MediaEntity): Long = (file.duration * file.playBackProgress * 1000).toLong()

    private fun getMediaProgress(file: MediaEntity, position: Long): Float =
        (position.toFloat() / (file.duration * 1000))


    fun play(file: MediaEntity, collectionId: Long) {
        val controller = this.controller ?: return
        if (controller.isPlaying) {
            if (PlayerData.currentPlayList[controller.currentMediaItemIndex] == file) {
                return
            }
        }
        val files = Files.getCollectionFiles(collectionId)
        if (PlayerData.currentCollection == collectionId && PlayerData.currentPlayList == files) {
            controller.seekTo(
                PlayerData.currentPlayList.indexOf(file),
                getMediaProgressMs(file)
            )
        } else {
            controller.setMediaItems(
                files.map { MediaItem.Builder().setMediaId(it.path).build() },
                files.indexOf(file),
                getMediaProgressMs(file)
            )
            PlayerData.setCollectionId(collectionId)
        }
        controller.setPlaybackSpeed(file.playBackSpeed)
        controller.repeatMode = if (file.playBackLoop) REPEAT_MODE_ONE else REPEAT_MODE_OFF
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

    fun playLastPlayed(folder: FolderEntity) {
        if (folder.lastPlayedId <= 0) {
            Log.d(TAG, "playLastPlayed: last player not present for folder $folder")
            return
        }
        val lastPlayedFile = Files.getFile(folder.lastPlayedId)
        Log.d(TAG, "onPlayClicked: lastPlayed - $lastPlayedFile")
        play(lastPlayedFile, folder.id)
    }

    fun saveProgress(mediaIndex: Int, position: Long) {
        try {
            val endMedia = PlayerData.currentPlayList[mediaIndex]
            endMedia.playBackProgress = getMediaProgress(endMedia, position)
            Files.update(endMedia)
        } catch (e: Exception) {
            Log.e(TAG, "saveProgress: error ", e)
        }
    }
}