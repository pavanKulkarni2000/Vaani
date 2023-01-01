package com.vaani.ui.folderMediaList

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaani.R
import com.vaani.models.File
import com.vaani.models.Folder
import com.vaani.ui.home.favouriteList.FavouriteViewModel
import com.vaani.ui.player.VlcPlayerFragment
import com.vaani.util.EmptyItemDecoration
import com.vaani.util.TAG
import io.objectbox.exception.UniqueViolationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class FolderMediaListFragment(private val currentFolder: Folder) : Fragment(R.layout.list_layout) {

    private lateinit var refreshLayout: SwipeRefreshLayout
    private val folderMediaViewModel: FolderMediaViewModel by viewModels {
        FolderMediaViewModel.Factory(requireActivity().application, currentFolder)
    }
    private val favouriteViewModel: FavouriteViewModel by viewModels()
    private val job = Job()
    private val scope = CoroutineScope(job)
    private val fileCallbacks = object : FileCallbacks {
        override fun onFavourite(file: File) {
            favouriteViewModel.addFavourite(file)
        }

        override fun onClick(file: File) {
            parentFragmentManager.commit {
                add(R.id.fragment_container_view, VlcPlayerFragment(file), TAG)
                addToBackStack(null)
            }
        }

        override fun onOptions(file: File, view: View) {
            val popup = PopupMenu(context!!, view)
            popup.menu.add(getString(R.string.favourite_selector_label)).apply {
                setIcon(R.drawable.foldermedia_favorite_filled_24px)
                setOnMenuItemClickListener {
                    try {
                        favouriteViewModel.addFavourite(file)
                    }catch (_:UniqueViolationException){
                        Toast.makeText(requireContext(), "Already favourite", Toast.LENGTH_SHORT).show()
                    }
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

    private fun initChildViews(view: View) {
        val adapter = FileAdapter(folderMediaViewModel.folderMediaList.value ?: emptyList(), fileCallbacks)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(EmptyItemDecoration())
        folderMediaViewModel.folderMediaList.observe(viewLifecycleOwner, adapter::updateList)

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            playMedia()
        }

        refreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        refreshLayout.setOnRefreshListener {
            scope.launch {
                folderMediaViewModel.updateFolderMedia()
                launch(Dispatchers.Main) {
                    refreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun playMedia() {
        TODO("Not yet implemented")
    }
}