package com.vaani.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.search.SearchView
import com.vaani.R
import com.vaani.VaaniApplication
import com.vaani.model.Folder
import com.vaani.player.PlayerUtil
import com.vaani.ui.util.GlobalMediaSearcher
import com.vaani.util.PreferenceUtil

@UnstableApi
class FolderFragment : BaseFragment<Folder>(R.layout.fragment_folders) {

    private val viewModel: FolderViewModel by viewModels {
        FolderViewModelFactory((requireActivity().application as VaaniApplication).container.fileManager)
    }
    private lateinit var globalMediaSearcher: GlobalMediaSearcher
    private lateinit var playerUtil: PlayerUtil

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val searchView: SearchView = view.findViewById(R.id.fragment_searchview)
        val searchContent: RecyclerView = view.findViewById(R.id.search_view_contents)
        val container = (requireActivity().application as VaaniApplication).container
        val fileManager = container.fileManager
        globalMediaSearcher = GlobalMediaSearcher(fileManager)
        globalMediaSearcher.setUp(searchView, searchContent)
        playerUtil = container.playerUtil

        viewModel.folders.observe(viewLifecycleOwner) {
            resetData(it)
            adapter.notifyDataSetChanged()
            stopRefreshLayout()
        }

        viewModel.loadFolders()
    }

    override fun onRefresh() {
        viewModel.refreshFolders()
    }

    override fun onItemLongClick(position: Int, view: View?): Boolean {
        // no action
        return false
    }

    override fun fabAction(view: View?) {
        if (playerUtil.controller?.isPlaying != true) {
            val idx = displayList.indexOfFirst { it.id == PreferenceUtil.lastPlayedFolderId }
            if (idx != -1) {
                openFolder(MediasFragment.newInstance(displayList[idx], true))
            }
        } else {
            playerUtil.startPlayerActivity()
        }
    }

    override fun onItemClick(position: Int, view: View?) = openFolder(MediasFragment.newInstance(displayList[position], false))

    private fun openFolder(fragment: MediasFragment) {
        requireActivity().supportFragmentManager.commit {
            add(R.id.main_activity_fragment_container_view, fragment)
            addToBackStack(null)
        }
    }

    companion object {
        fun newInstance() = FolderFragment()
    }
}
