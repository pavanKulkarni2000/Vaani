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
import com.vaani.ui.EmptyItemDecoration
import com.vaani.ui.files.FileListFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@UnstableApi
class FolderListFragment : Fragment(R.layout.list_layout) {

    private lateinit var refreshLayout: SwipeRefreshLayout
    private val job = Job()
    private val scope = CoroutineScope(job + Dispatchers.IO)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initChildViews(view)
    }

    private fun initChildViews(view: View) {

        val adapter = FolderAdapter(requireContext(), Files.allFolders, ::changeDirectory)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(EmptyItemDecoration())
        Files.allFoldersLive.observe(viewLifecycleOwner, adapter::updateList)

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            onPlayClicked()
        }

        refreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        refreshLayout.setOnRefreshListener {
            scope.launch {
                Files.explore()
                with(Dispatchers.Main) {
                    refreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun onPlayClicked() {

    }

    private fun changeDirectory(folderEntity: FolderEntity) {
        Files.changeCurrentFolder(folderEntity)
        requireParentFragment().parentFragmentManager.commit {
            add(R.id.fragment_container_view, FileListFragment::class.java,Bundle.EMPTY)
            addToBackStack(null)
        }
    }

}