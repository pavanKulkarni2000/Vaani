package com.vaani.ui.files

import android.content.ComponentName
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.common.util.concurrent.ListenableFuture
import com.vaani.R
import com.vaani.data.Files
import com.vaani.models.FileEntity
import com.vaani.models.FolderEntity
import com.vaani.ui.player.PlaybackService
import com.vaani.ui.EmptyItemDecoration
import com.vaani.ui.player.PlayerFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@UnstableApi
class FileListFragment(private val currentFolderEntity: FolderEntity) : Fragment(R.layout.list_layout) {


    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null
    private lateinit var refreshLayout: SwipeRefreshLayout
    private val fileCallbacks = object : FileCallbacks {
        override fun onClick(file: FileEntity) {
            controller?.run{
                setMediaItem(MediaItem.Builder().setMediaId(file.path).build())
                setPlaybackSpeed(file.playBackSpeed)
                repeatMode = if(file.playBackLoop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_ALL
                shuffleModeEnabled = false
                prepare()
                play()
            }
            parentFragmentManager.commit {
                add(R.id.fragment_container_view, PlayerFragment::class.java, Bundle.EMPTY)
                addToBackStack(null)
            }
        }

        override fun onOptions(file: FileEntity, view: View) {
            val popup = PopupMenu(context!!, view)
            popup.menu.add(getString(R.string.favourite_add_label)).apply {
                setIcon(R.drawable.foldermedia_favorite_filled_24px)
                setOnMenuItemClickListener {
                    Files.addFavourite(file)
                    true
                }
            }
            popup.show()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initChildViews(view)
    }

    override fun onStart() {
        super.onStart()
        initializeController()
    }

    override fun onStop() {
        super.onStop()
        releaseController()
    }

    private fun initializeController() {
        controllerFuture =
            MediaController.Builder(
                requireContext(),
                SessionToken(requireContext(), ComponentName(requireContext(), PlaybackService::class.java))
            )
                .buildAsync()
    }

    private fun releaseController() {
        MediaController.releaseFuture(controllerFuture)
    }

    private fun initChildViews(view: View) {
        val adapter = FileAdapter(Files.currentFiles, fileCallbacks)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(EmptyItemDecoration())
        Files.currentFilesLive.observe(viewLifecycleOwner, adapter::updateList)

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            onPlayClicked()
        }

        refreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        refreshLayout.setOnRefreshListener {
            CoroutineScope(Job()).launch {
                Files.exploreFolder(currentFolderEntity)
                launch(Dispatchers.Main) {
                    refreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun onPlayClicked() {
    }
}