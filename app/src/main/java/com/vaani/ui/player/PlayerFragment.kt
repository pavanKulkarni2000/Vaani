package com.vaani.ui.player

import android.content.ComponentName
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
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
import com.vaani.R
import com.vaani.data.PlayerData
import com.vaani.player.PlaybackService
import com.vaani.util.TAG


@UnstableApi class PlayerFragment :
    Fragment(R.layout.player_layout){
    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null
    private lateinit var playerView: PlayerView
    private lateinit var shuffleButton: ImageView
    private lateinit var repeatButton: ImageView
    private lateinit var infoTitle: TextView
    private lateinit var infoAlbum: TextView
    private lateinit var infoArtist: TextView
    private lateinit var infoGenre: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initControls(view)
        shuffleButton.setOnClickListener {
            val controller = this.controller ?: return@setOnClickListener
            controller.shuffleModeEnabled = !controller.shuffleModeEnabled
        }
        repeatButton.setOnClickListener {
            val controller = this.controller ?: return@setOnClickListener
            when (controller.repeatMode) {
                REPEAT_MODE_ALL -> controller.repeatMode = REPEAT_MODE_ONE
                REPEAT_MODE_ONE -> controller.repeatMode = REPEAT_MODE_ALL
                REPEAT_MODE_OFF -> controller.repeatMode = REPEAT_MODE_ONE
            }
        }
    }

    private fun initControls( view: View) {
        playerView = view.findViewById(R.id.player_view)
        shuffleButton = view.findViewById(R.id.shuffle_switch)
        repeatButton = view.findViewById(R.id.repeat_switch)
        infoAlbum = view.findViewById(R.id.video_artist)
        infoGenre = view.findViewById(R.id.video_genre)
        infoTitle = view.findViewById(R.id.video_title)
        infoArtist = view.findViewById(R.id.video_album)
    }

    override fun onStart() {
        Log.d(TAG, "onStart: started")
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
        controllerFuture.addListener({ setController()}, MoreExecutors.directExecutor())
    }

    private fun setController() {
        val controller = this.controller ?: return

        playerView.player = controller

        updateMediaMetadataUI(controller.mediaMetadata)
        updateShuffleSwitchUI(controller.shuffleModeEnabled)
        updateRepeatSwitchUI(controller.repeatMode)
        playerView.setShowSubtitleButton(controller.currentTracks.isTypeSupported(C.TRACK_TYPE_TEXT))

        controller.addListener(
            object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    // not working
//                    updateMediaMetadataUI(mediaItem?.mediaMetadata ?: MediaMetadata.EMPTY)
                }

                override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                    updateShuffleSwitchUI(shuffleModeEnabled)
                }

                override fun onRepeatModeChanged(repeatMode: Int) {
                    updateRepeatSwitchUI(repeatMode)
                }

                override fun onTracksChanged(tracks: Tracks) {
                    playerView.setShowSubtitleButton(tracks.isTypeSupported(C.TRACK_TYPE_TEXT))
                    updateMediaMetadataUI(PlayerData.getMetaData(controller.currentMediaItemIndex))
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
        infoTitle.text = mediaMetadata.title ?: getString(R.string.no_item_prompt)
        infoAlbum.text = mediaMetadata.albumTitle ?: "No Album info"
        infoArtist.text = mediaMetadata.artist ?: "No Artist info"
        infoGenre.text = mediaMetadata.genre ?: "No Genre info"

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
