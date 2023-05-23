package com.vaani.data

import androidx.lifecycle.MutableLiveData
import com.vaani.db.DB
import com.vaani.models.FileEntity
import com.vaani.models.FolderEntity
import com.vaani.player.Player
import com.vaani.util.Constants
import com.vaani.util.PreferenceUtil

object PlayerState {

     val isPlayingLive = MutableLiveData(false)
     val speedLive= MutableLiveData(1f)
     val loopLive= MutableLiveData(false)
     val shuffleLive= MutableLiveData(false)
    val fileLive= MutableLiveData(FileEntity())
     val folderLive= MutableLiveData(FolderEntity())
     val isAttachedLive= MutableLiveData(false)

    val isPlaying: Boolean
        get() = isPlayingLive.value!!

    val speed: Float
        get() = speedLive.value!!

    val loop: Boolean
        get() = loopLive.value!!

    val shuffle: Boolean
        get() = shuffleLive.value!!

    val file: FileEntity
        get() = fileLive.value!!

    val folder: FolderEntity
        get() = folderLive.value!!

    val isAttached: Boolean
        get() = isAttachedLive.value!!

    fun setCurrentFile(newFile: FileEntity) {
        if (file.id == newFile.id)
            return
        if (file.folderId != newFile.folderId) {
            folderLive.value = Files.getFolder(newFile.folderId)
            shuffleLive.value = folder.playBackShuffle
        }
        fileLive.value = newFile
        speedLive.value = newFile.playBackSpeed
        loopLive.value = newFile.playBackLoop
    }

    fun updateSpeed(newSpeed: Float) {
        speedLive.value = newSpeed
        file.playBackSpeed = newSpeed
        Files.updateFile(file)
    }

    fun setAttached(attached: Boolean) {
        isAttachedLive.value = attached
    }

    fun updateLoop(loopSwitch: Boolean) {
        loopLive.value = loopSwitch
        file.playBackLoop = loopSwitch
        Files.updateFile(file)
    }

    fun updateShuffle(shuffleSwitch: Boolean) {
        shuffleLive.value = shuffleSwitch
        folder.playBackShuffle = shuffleSwitch
        Files.updateFolder(folder)
    }

    fun updatePlaying(isPlayingSwitch: Boolean) {
        isPlayingLive.value = isPlayingSwitch
    }

    fun savePreference() {
        if (file.id > 0) {
            file.playBackProgress = Player.currentPosition/Player.duration.toFloat()
            Files.updateFile(file)
            folder.lastPlayedId = file.id
            if(file.folderId!=Constants.FAVOURITE_COLLECTION_ID){
                Files.updateFolder(folder)
            }
            PreferenceUtil.lastPlayedFolderId=file.folderId
            PreferenceUtil.save()
        }
    }

}