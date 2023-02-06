package com.vaani.ui.home.favouriteList

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaani.R
import com.vaani.models.Favourite
import com.vaani.ui.player.VlcPlayerFragment
import com.vaani.util.EmptyItemDecoration
import com.vaani.util.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job


class FavouriteMediaListFragment : Fragment(R.layout.list_layout) {

    private lateinit var refreshLayout: SwipeRefreshLayout
    private val favouriteViewModel: FavouriteViewModel by viewModels {
        FavouriteViewModel.Factory(requireActivity().application)
    }
    private val job = Job()
    private val scope = CoroutineScope(job)
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
            playMedia()
        }

        refreshLayout = view.findViewById(R.id.swipe_refresh_layout)
//        refreshLayout.setOnRefreshListener(::refreshData)
    }

    private fun playMedia() {
        TODO("Not yet implemented")
    }

}