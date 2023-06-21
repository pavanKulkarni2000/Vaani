package com.vaani.ui.favourites

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaani.R
import com.vaani.data.Files
import com.vaani.models.FileEntity
import com.vaani.player.Player
import com.vaani.ui.EmptyItemDecoration
import com.vaani.ui.files.FileAdapter
import com.vaani.ui.files.FileCallbacks
import com.vaani.ui.player.PlayerFragment
import com.vaani.util.TAG


class FavouriteMediaListFragment : Fragment(R.layout.list_layout) {

    private lateinit var refreshLayout: SwipeRefreshLayout
    private val favouriteCallbacks = object : FileCallbacks {
        override fun onClick(file: FileEntity) {
            Player.startNewMedia(file)
            requireParentFragment().parentFragmentManager.commit {
                add(R.id.fragment_container_view, PlayerFragment::class.java, null, TAG)
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
//        val file: FileEntity? =
//            if (Player.state.isPlaying.value!! && Player.state.file.value?.folderId == FAVOURITE_COLLECTION_ID) {
//                Player.state.file.value
//            } else {
//                Log.d(TAG, "onPlayClicked: ${PreferenceUtil.Favourite.lastPlayedId}")
//                favouriteViewModel.favouriteEntityMediaList.value?.find { it.file.target.id == PreferenceUtil.Favourite.lastPlayedId }?.file?.target
//            }
//        file?.let {
//            Player.startNewMedia(it)
//            parentFragmentManager.commit {
//                add(R.id.fragment_container_view, VlcPlayerFragment::class.java, null, TAG)
//                addToBackStack(null)
//            }
//        }
    }

}