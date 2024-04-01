package com.vaani.ui.player

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate.FEATURE_SUPPORT_ACTION_BAR_OVERLAY
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.common.util.RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL
import androidx.media3.common.util.RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.vaani.R
import com.vaani.data.PlayerData
import com.vaani.player.PlaybackService
import com.vaani.util.TAG

@UnstableApi
class PlayerActivity :
  AppCompatActivity(R.layout.player_activity), PlayerView.ControllerVisibilityListener {
  private lateinit var controllerFuture: ListenableFuture<MediaController>
  private val controller: MediaController?
    get() = if (controllerFuture.isDone) controllerFuture.get() else null

  private lateinit var playerView: PlayerView

  override fun onCreate(savedInstanceState: Bundle?) {
    supportRequestWindowFeature(FEATURE_SUPPORT_ACTION_BAR_OVERLAY)
    super.onCreate(savedInstanceState)
    playerView = findViewById(R.id.player_view)
    playerView.setControllerVisibilityListener(this)
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.player_activity_menu, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      R.id.player_action_info -> Log.d(TAG, "onOptionsItemSelected: info")
    }
    return super.onOptionsItemSelected(item)
  }

  override fun onStart() {
    super.onStart()
    initializeController()
  }

  private fun initializeController() {
    controllerFuture =
      MediaController.Builder(
          this,
          SessionToken(this, ComponentName(this, PlaybackService::class.java))
        )
        .buildAsync()
    controllerFuture.addListener({ setController() }, MoreExecutors.directExecutor())
  }

  private fun setController() {
    val controller = this.controller ?: return

    playerView.player = controller
    playerView.setShowSubtitleButton(false)
    playerView.setShowShuffleButton(true)
    playerView.setRepeatToggleModes(REPEAT_TOGGLE_MODE_ALL or REPEAT_TOGGLE_MODE_ONE)

    updateMediaMetadataUI()
    controller.addListener(
      object : Player.Listener {
        //                override fun onMediaItemTransition(mediaItem: Media?, reason: Int) {
        //                    // not working
        //                    updateMediaMetadataUI(mediaItem?.mediaMetadata ?: MediaMetadata.EMPTY)
        //                }
        //
        //                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        //                    updateShuffleSwitchUI(shuffleModeEnabled)
        //                }
        //
        //                override fun onRepeatModeChanged(repeatMode: Int) {
        //                    updateRepeatSwitchUI(repeatMode)
        //                }

        override fun onTracksChanged(tracks: Tracks) {
          playerView.setShowSubtitleButton(tracks.isTypeSupported(C.TRACK_TYPE_TEXT))
          updateMediaMetadataUI()
        }
      }
    )
  }

  private fun updateMediaMetadataUI() {
    supportActionBar?.subtitle =
      PlayerData.currentPlayList[controller?.currentMediaItemIndex!!].name
  }

  override fun onStop() {
    super.onStop()
    playerView.player = null
    releaseController()
  }

  private fun releaseController() {
    MediaController.releaseFuture(controllerFuture)
  }

  override fun onVisibilityChanged(visibility: Int) {
    supportActionBar?.let {
      if (visibility == View.VISIBLE) {
        it.show()
      } else {
        it.hide()
      }
    }
  }
}
