package com.vaani.ui.files

import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaani.R
import com.vaani.data.Files
import com.vaani.models.FileEntity
import com.vaani.models.FolderEntity
import com.vaani.ui.EmptyItemDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@UnstableApi
class FileListFragment : Fragment(R.layout.list_layout) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initChildViews(view)
    }

    private fun initChildViews(view: View) {

        val fileCallbacks = object : FileCallbacks(Files.currentFolder.id,parentFragmentManager) {

            override fun onOptions(file: FileEntity, view: View) {
                val popup = PopupMenu(context!!, view)
                popup.menu.add(getString(R.string.favourite_add_label)).apply {
                    setIcon(R.drawable.foldermedia_favorite_filled_24px)
                    setOnMenuItemClickListener {
                        Files.addFavourite(file)
                        true
                    }
                }
                popup.show()
            }

        }

        val adapter = FileAdapter(Files.currentFiles, fileCallbacks)
        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(EmptyItemDecoration())
        Files.currentFilesLive.observe(viewLifecycleOwner, adapter::updateList)

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            fileCallbacks.onPlayClicked()
        }

        val refreshLayout:SwipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        refreshLayout.setOnRefreshListener {
            CoroutineScope(Job()).launch {
                Files.exploreFolder(Files.currentFolder)
                launch(Dispatchers.Main) {
                    refreshLayout.isRefreshing = false
                }
            }
        }
    }

}