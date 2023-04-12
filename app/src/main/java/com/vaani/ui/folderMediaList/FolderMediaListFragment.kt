package com.vaani.ui.folderMediaList

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaani.R
import com.vaani.db.DB
import com.vaani.models.File
import com.vaani.models.Folder
import com.vaani.ui.favouriteList.FavouriteViewModel
import com.vaani.player.Player
import com.vaani.ui.player.VlcPlayerFragment
import com.vaani.util.EmptyItemDecoration
import com.vaani.util.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


class FolderMediaListFragment(private val currentFolder: Folder) : Fragment(R.layout.list_layout) {

    private lateinit var refreshLayout: SwipeRefreshLayout
    private val folderMediaViewModel: FolderMediaViewModel by viewModels {
        FolderMediaViewModel.Factory(requireActivity().application, currentFolder)
    }
    private val favouriteViewModel: FavouriteViewModel by viewModels {
        FavouriteViewModel.Factory(requireActivity().application)
    }
    private val job = Job()
    private val scope = CoroutineScope(job)
    private val fileCallbacks = object : FileCallbacks {
        override fun onClick(file: File) {
            Player.startNewMedia(file)
            parentFragmentManager.commit {
                add(R.id.fragment_container_view, VlcPlayerFragment::class.java, null, TAG)
                addToBackStack(null)
            }
        }

        override fun onOptions(file: File, view: View) {
            val popup = PopupMenu(context!!, view)
            popup.menu.add(getString(R.string.favourite_selector_label)).apply {
                setIcon(R.drawable.foldermedia_favorite_filled_24px)
                setOnMenuItemClickListener {
                    favouriteViewModel.addFavourite(file)
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
            onPlayClicked()
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

    private fun onPlayClicked() {
        var file: File? = null
        if (Player.state.isPlaying.value!! && Player.state.file.value?.folderId == currentFolder.id) {
            file = Player.state.file.value
        } else {
            DB.CRUD.getCollectionPreference(currentFolder.id)?.let { collPref ->
                file = folderMediaViewModel.folderMediaList.value?.find { it.id == collPref.lastPlayedId }
            }
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