package com.vaani.ui.player

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player.REPEAT_MODE_ALL
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.REPEAT_MODE_ONE
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.media3.ui.PlayerView
import androidx.media3.ui.R as media3R
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.vaani.MainActivity
import com.vaani.R
import com.vaani.data.PlayerState
import com.vaani.models.FileEntity
import com.vaani.player.PlaybackService
import com.vaani.player.Player
import com.vaani.util.TAG


@UnstableApi class PlayerFragment :
    Fragment(R.layout.player_layout){
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null
    private lateinit var playerView: PlayerView
    private lateinit var shuffleButton: ImageView
    private lateinit var repeatButton: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerView = view.findViewById(R.id.player_view)
        shuffleButton = view.findViewById(R.id.shuffle_switch)
        shuffleButton.setOnClickListener {
            val controller = this.controller ?: return@setOnClickListener
            controller.shuffleModeEnabled = !controller.shuffleModeEnabled
        }

        view.findViewById<ImageView>(R.id.repeat_switch).setOnClickListener {
            val controller = this.controller ?: return@setOnClickListener
            when (controller.repeatMode) {
                REPEAT_MODE_ALL -> controller.repeatMode = REPEAT_MODE_ONE
                REPEAT_MODE_ONE -> controller.repeatMode = REPEAT_MODE_ALL
                REPEAT_MODE_OFF -> {
                    TODO()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        initializeController()
    }

    private fun initializeController() {
        controllerFuture =
            MediaController.Builder(
                requireContext(),
                SessionToken(requireContext(), ComponentName(requireContext(), PlaybackService::class.java))
            )
                .buildAsync()
        controllerFuture.addListener({ setController() }, MoreExecutors.directExecutor())
    }

    private fun setController() {
        val controller = this.controller ?: return

        playerView.player = controller

        updateMediaMetadataUI(controller.mediaMetadata)
        updateShuffleSwitchUI(controller.shuffleModeEnabled)
        updateRepeatSwitchUI(controller.repeatMode)
        playerView.setShowSubtitleButton(controller.currentTracks.isTypeSupported(C.TRACK_TYPE_TEXT))

        controller.addListener(
            object : androidx.media3.common.Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    updateMediaMetadataUI(mediaItem?.mediaMetadata ?: MediaMetadata.EMPTY)
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    updateShuffleSwitchUI(shuffleModeEnabled)
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    updateRepeatSwitchUI(repeatMode)
                }

                override fun onTracksChanged(tracks: Tracks) {
                    playerView.setShowSubtitleButton(tracks.isTypeSupported(C.TRACK_TYPE_TEXT))
                }
            }
        )
    }

    private fun updateShuffleSwitchUI(shuffleModeEnabled: Boolean) {
        val resId =
            if (shuffleModeEnabled) media3R.drawable.exo_styled_controls_shuffle_on
            else media3R.drawable.exo_styled_controls_shuffle_off
       shuffleButton
            .setImageDrawable(ContextCompat.getDrawable(requireContext(), resId))
    }

    private fun updateRepeatSwitchUI(repeatMode: Int) {
        val resId: Int =
            when (repeatMode) {
                REPEAT_MODE_ONE -> media3R.drawable.exo_styled_controls_repeat_one
                REPEAT_MODE_ALL -> media3R.drawable.exo_styled_controls_repeat_all
                else -> media3R.drawable.exo_styled_controls_repeat_off
            }
        repeatButton
            .setImageDrawable(ContextCompat.getDrawable(requireContext(), resId))
    }

    private fun updateMediaMetadataUI(mediaMetadata: MediaMetadata) {
        val title: CharSequence = mediaMetadata.title ?: getString(R.string.no_item_prompt)

        playerView.findViewById<TextView>(R.id.video_title).text = title
        playerView.findViewById<TextView>(R.id.video_album).text = mediaMetadata.albumTitle
        playerView.findViewById<TextView>(R.id.video_artist).text = mediaMetadata.artist
        playerView.findViewById<TextView>(R.id.video_genre).text = mediaMetadata.genre

    }

    override fun onStop() {
        super.onStop()
        playerView.player = null
        releaseController()
    }

    private fun releaseController() {
        MediaController.releaseFuture(controllerFuture)
    }
}
