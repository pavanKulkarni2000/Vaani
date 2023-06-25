package com.vaani.ui.favourites

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
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.common.util.concurrent.ListenableFuture
import com.vaani.R
import com.vaani.data.Files
import com.vaani.models.FileEntity
import com.vaani.player.PlaybackService
import com.vaani.ui.EmptyItemDecoration
import com.vaani.ui.files.FileAdapter
import com.vaani.ui.files.FileCallbacks
import com.vaani.ui.player.PlayerFragment
import com.vaani.util.TAG

@UnstableApi
class FavouriteMediaListFragment : Fragment(R.layout.list_layout) {

    private lateinit var controllerFuture: ListenableFuture<MediaController>
    private val controller: MediaController?
        get() = if (controllerFuture.isDone) controllerFuture.get() else null
    private lateinit var refreshLayout: SwipeRefreshLayout
    private val favouriteCallbacks = object : FileCallbacks {
        override fun onClick(file: FileEntity) {
//            PlayerData.update(file,Files.favourites)
            controller?.run{
                setMediaItem(MediaItem.Builder().setMediaId(file.path).build())
                setPlaybackSpeed(file.playBackSpeed)
                repeatMode = if(file.playBackLoop) Player.REPEAT_MODE_ONE else Player.REPEAT_MODE_ALL
                shuffleModeEnabled = false
                prepare()
                play()
            }
            requireParentFragment().parentFragmentManager.commit {
                add(R.id.fragment_container_view, PlayerFragment::class.java, Bundle.EMPTY)
                addToBackStack(null)
            }
        }

        override fun onOptions(file: FileEntity, view: View) {
            val popup = PopupMenu(context!!, view)
            popup.menu.add(getString(R.string.favourite_remove_label)).apply {
                setIcon(R.drawable.foldermedia_favorite_24px)
                setOnMenuItemClickListener {
                    Files.removeFavourite(file)
                    true
                }
            }
            popup.show()
        }
    }

    private val touchHelper = object : ItemTouchHelper.SimpleCallback(UP or DOWN, START or END) {
        override fun onMoved(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            fromPos: Int,
            target: RecyclerView.ViewHolder,
            toPos: Int,
            x: Int,
            y: Int
        ) {
            val from = viewHolder.adapterPosition
            val to = target.adapterPosition
            Files.moveFavourite(from, to)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        }

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initChildViews(view)
    }


    private fun initChildViews(view: View) {
        val adapter =
            FileAdapter(Files.favourites, favouriteCallbacks)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(EmptyItemDecoration())
        ItemTouchHelper(touchHelper).attachToRecyclerView(recyclerView)
        Files.favouritesLive.observe(viewLifecycleOwner, adapter::updateList)

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            onPlayClicked()
        }

        refreshLayout = view.findViewById(R.id.swipe_refresh_layout)
//        refreshLayout.setOnRefreshListener(::refreshData)
    }

    private fun onPlayClicked() {
    }

}