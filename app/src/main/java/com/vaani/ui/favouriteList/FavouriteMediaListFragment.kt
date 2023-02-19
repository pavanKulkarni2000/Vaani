package com.vaani.ui.favouriteList

import android .os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaani.R
import com.vaani.models.Favourite
import com.vaani.models.File
import com.vaani.ui.player.Player
import com.vaani.ui.player.VlcPlayerFragment
import com.vaani.util.Constants.FAVOURITE_COLLECTION_ID
import com.vaani.util.EmptyItemDecoration
import com.vaani.util.PreferenceUtil
import com.vaani.util.TAG
import kotlinx.coroutines.Job


class FavouriteMediaListFragment : Fragment(R.layout.list_layout) {

    private lateinit var refreshLayout: SwipeRefreshLayout
    private val favouriteViewModel: FavouriteViewModel by viewModels {
        FavouriteViewModel.Factory(requireActivity().application)
    }
    private val job = Job()
    private val favouriteCallbacks = object : FavouriteCallbacks {
        override fun onClick(favourite: Favourite) {
            requireParentFragment().parentFragmentManager.commit {
                add(R.id.fragment_container_view, VlcPlayerFragment(favourite.file.target), TAG)
                addToBackStack(null)
            }
        }

        override fun onFavRemove(favourite: Favourite) {
            favouriteViewModel.removeFavourite(favourite)
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
        favouriteViewModel.favouriteMediaList.observe(viewLifecycleOwner, adapter::updateList)

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            onPlayClicked()
        }

        refreshLayout = view.findViewById(R.id.swipe_refresh_layout)
//        refreshLayout.setOnRefreshListener(::refreshData)
    }

    private fun onPlayClicked() {
        val file: File? = if(Player.mediaPlayerService.isPlaying && Player.mediaPlayerService.currentMediaFile?.folderId ==FAVOURITE_COLLECTION_ID){
            Player.mediaPlayerService.currentMediaFile
        } else {
            Log.d(TAG, "onPlayClicked: ${PreferenceUtil.Favourite.lastPlayedId}")
            favouriteViewModel.favouriteMediaList.value?.find { it.file.target.id == PreferenceUtil.Favourite.lastPlayedId}?.file?.target
        }
        file?.let{
            parentFragmentManager.commit {
                add(R.id.fragment_container_view, VlcPlayerFragment(it), TAG)
                addToBackStack(null)
            }
        }
    }

}