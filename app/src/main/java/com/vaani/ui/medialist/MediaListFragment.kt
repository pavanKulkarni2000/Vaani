package com.vaani.ui.medialist

import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaani.R
import com.vaani.data.Files
import com.vaani.data.PlayerData
import com.vaani.models.FileEntity
import com.vaani.models.FolderEntity
import com.vaani.player.PlayerUtil
import com.vaani.ui.EmptyItemDecoration

@UnstableApi
abstract class MediaListFragment(protected val folder: FolderEntity) : Fragment(R.layout.list_layout),
    MediaItemCallbacks {

    protected lateinit var mediaListAdapter: MediaListAdapter
    protected lateinit var recyclerView: RecyclerView
    protected lateinit var refreshLayout: SwipeRefreshLayout
    abstract val refreshMediaList: SwipeRefreshLayout.OnRefreshListener
    abstract val menuProvider: MenuProvider

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mediaListAdapter = MediaListAdapter(Files.getFolderFiles(folder.id), this)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = mediaListAdapter
        recyclerView.addItemDecoration(EmptyItemDecoration())

        val fab: FloatingActionButton = view.findViewById(R.id.play_fab)
        fab.setOnClickListener {
            folderPlay()
        }

        refreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        refreshLayout.setOnRefreshListener(refreshMediaList)

        requireActivity().addMenuProvider(menuProvider)
    }

    override fun onClick(file: FileEntity) {
        PlayerUtil.play(file)
    }

    private fun folderPlay() {
        if (PlayerUtil.controller?.isPlaying == false || PlayerData.currentCollection != folder.id) {
            PlayerUtil.playLastPlayed(folder)
        } else {
            PlayerUtil.startPlayerActivity()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().removeMenuProvider(menuProvider)
    }

    protected val searchQuery = object : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
            Files.search(folder, query)
            mediaListAdapter.notifyDataSetChanged()
            return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
            Files.search(folder, newText)
            mediaListAdapter.notifyDataSetChanged()
            return true
        }
    }

    protected val searchClose = SearchView.OnCloseListener {
        Files.search(folder, null)
        mediaListAdapter.notifyDataSetChanged()
        true
    }
}