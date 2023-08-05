package com.vaani.ui.folderList

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaani.R
import com.vaani.data.Files
import com.vaani.models.FolderEntity
import com.vaani.player.PlayerUtil
import com.vaani.ui.EmptyItemDecoration
import com.vaani.ui.files.FileListFragment
import com.vaani.util.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@UnstableApi
class FolderListFragment : Fragment(R.layout.list_layout) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initChildViews(view)
    }

    private fun initChildViews(view: View) {

        val adapter = FolderAdapter(Files.allFolders, ::changeDirectory)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(EmptyItemDecoration())
        val fab: FloatingActionButton = view.findViewById(R.id.play_fab)
        fab.setOnClickListener {
            lastPlayedFolderPlay()
        }

        val refreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_layout)
        refreshLayout.setOnRefreshListener {
            CoroutineScope(Job()).launch {
                Files.explore()
                launch(Dispatchers.Main) {
                    adapter.notifyDataSetChanged()
                    refreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun lastPlayedFolderPlay() {
        if (PlayerUtil.controller?.isPlaying == false) {
            PlayerUtil.playLastPlayed(Files.getFolder(PreferenceUtil.lastPlayedFolderId))
        } else {
            PlayerUtil.startPlayerActivity()
        }
    }

    private fun changeDirectory(folderEntity: FolderEntity) {
        Files.setCurrentFolder(folderEntity)
        requireParentFragment().parentFragmentManager.commit {
            add(R.id.fragment_container_view, FileListFragment::class.java, Bundle.EMPTY)
            addToBackStack(null)
        }
    }

}