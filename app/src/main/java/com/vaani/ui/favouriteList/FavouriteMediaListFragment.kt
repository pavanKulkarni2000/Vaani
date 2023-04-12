package com.vaani.ui.favouriteList

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.START
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaani.R
import com.vaani.models.Favourite
import com.vaani.models.File
import com.vaani.player.Player
import com.vaani.ui.player.VlcPlayerFragment
import com.vaani.util.Constants.FAVOURITE_COLLECTION_ID
import com.vaani.util.EmptyItemDecoration
import com.vaani.util.PreferenceUtil
import com.vaani.util.TAG


class FavouriteMediaListFragment : Fragment(R.layout.list_layout) {

    private lateinit var refreshLayout: SwipeRefreshLayout
    private val favouriteViewModel: FavouriteViewModel by viewModels {
        FavouriteViewModel.Factory(requireActivity().application)
    }
    private val favouriteCallbacks = object : FavouriteCallbacks {
        override fun onClick(favourite: Favourite) {
            Player.startNewMedia(favourite.file.target)
            requireParentFragment().parentFragmentManager.commit {
                add(R.id.fragment_container_view, VlcPlayerFragment::class.java, null, TAG)
                addToBackStack(null)
            }
        }

        override fun onFavRemove(favourite: Favourite) {
            favouriteViewModel.removeFavourite(favourite)
        }

    }
    private val touchHelper = object : ItemTouchHelper.SimpleCallback(UP or DOWN, START or END) {
        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.adapterPosition
            val to = target.adapterPosition
            favouriteViewModel.updateRank(from, to)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            TODO("Not yet implemented")
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initChildViews(view)
    }


    private fun initChildViews(view: View) {
        val adapter =
            FavouriteAdapter(favouriteViewModel.favouriteMediaList.value ?: emptyList(), favouriteCallbacks)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(EmptyItemDecoration())
        ItemTouchHelper(touchHelper).attachToRecyclerView(recyclerView)
        favouriteViewModel.favouriteMediaList.observe(viewLifecycleOwner, adapter::updateList)

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            onPlayClicked()
        }

        refreshLayout = view.findViewById(R.id.swipe_refresh_layout)
//        refreshLayout.setOnRefreshListener(::refreshData)
    }

    private fun onPlayClicked() {
        val file: File? =
            if (Player.state.isPlaying.value!! && Player.state.file.value?.folderId == FAVOURITE_COLLECTION_ID) {
                Player.state.file.value
            } else {
                Log.d(TAG, "onPlayClicked: ${PreferenceUtil.Favourite.lastPlayedId}")
                favouriteViewModel.favouriteMediaList.value?.find { it.file.target.id == PreferenceUtil.Favourite.lastPlayedId }?.file?.target
            }
        file?.let {
            Player.startNewMedia(it)
            parentFragmentManager.commit {
                add(R.id.fragment_container_view, VlcPlayerFragment::class.java, null, TAG)
                addToBackStack(null)
            }
        }
    }

}