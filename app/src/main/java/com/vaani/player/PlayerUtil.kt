package com.vaani.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.vaani.MainActivity
import com.vaani.data.Files
import com.vaani.data.PlayerData
import com.vaani.models.FileEntity
import com.vaani.models.FolderEntity
import com.vaani.ui.player.PlayerActivity
import com.vaani.util.TAG

@UnstableApi
object PlayerUtil {

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null

    fun init(context: Context) {
        controllerFuture = MediaController.Builder(
            context,
            SessionToken(context, ComponentName(context, PlaybackService::class.java))
        ).buildAsync()
    }

    fun getMediaProgressMs(file: FileEntity): Long = (file.duration * file.playBackProgress * 1000).toLong()

    fun getMediaProgress(file: FileEntity, position: Long): Float = (position.toFloat() / (file.duration * 1000))


    fun play(file: FileEntity) {
        val controller = this.controller ?: return
        if (controller.isPlaying) {
            if (PlayerData.currentPlayList[controller.currentMediaItemIndex] == file) {
                return
            }
        }
        if (PlayerData.currentCollection == file.folderId) {
            controller.seekTo(
                PlayerData.currentPlayList.indexOf(file),
                getMediaProgressMs(file)
            )
        } else {
            PlayerData.setCollectionId(file.folderId)
            controller.setMediaItems(
                PlayerData.getMediaItems(),
                PlayerData.currentPlayList.indexOf(file),
                getMediaProgressMs(file)
            )
        }
//        controller.setPlaybackSpeed(file.playBackSpeed)
//        controller.repeatMode = if(file.playBackLoop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_ALL
//        controller.shuffleModeEnabled = Files.getFolder(file.folderId).playBackShuffle
        controller.prepare()
        controller.play()
    }

    fun startPlayerActivity() {
        MainActivity.context.startActivity(
            Intent(MainActivity.context, PlayerActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK)
        )
        controller?.play()
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
            return
        }
        val lastPlayedFile = Files.getFile(folder.lastPlayedId)
        Log.d(TAG, "onPlayClicked: lastPlayed - $lastPlayedFile")
        play(lastPlayedFile)
    }
}