package com.vaani.ui.files

import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.vaani.R
import com.vaani.data.Files
import com.vaani.data.PlayerData
import com.vaani.models.FileEntity
import com.vaani.player.PlaybackService
import com.vaani.player.PlayerUtil
import com.vaani.ui.player.PlayerFragment
import com.vaani.util.Constants.FAVOURITE_COLLECTION_ID
import com.vaani.util.PreferenceUtil
import com.vaani.util.TAG

@UnstableApi
abstract class FileCallbacks(private val folderId:Long,private val fragmentManager: FragmentManager) {

    private val controller = PlayerUtil.controller

    fun onClick(file: FileEntity,playList:List<FileEntity>) = play(file,playList)

    fun onPlayClicked() {
        val lastPlayedId = Files.getFolder(folderId).lastPlayedId
        if(lastPlayedId<=0)
            return
        val lastPlayedFile = Files.getFile(lastPlayedId)
        Log.d(TAG, "onPlayClicked: lastPlayed - $lastPlayedFile")
        play(lastPlayedFile,Files.getFolderFiles(folderId))
    }

    abstract fun onOptions(file: FileEntity, view: View)

    private fun play(file: FileEntity,playList:List<FileEntity>){
        controller?.run{
            setMediaItems( PlayerData.setPlayList(playList),playList.indexOf(file),
                (file.duration*file.playBackProgress*1000).toLong()
            )
            setPlaybackSpeed(file.playBackSpeed)
            repeatMode = if(file.playBackLoop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_ALL
            shuffleModeEnabled = Files.getFolder(folderId).playBackShuffle
            prepare()
            play()
        }
        fragmentManager.commit {
            add(R.id.fragment_container_view, PlayerFragment::class.java, Bundle.EMPTY)
            addToBackStack(null)
        }

    }

}