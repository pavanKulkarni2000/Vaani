package com.vaani.ui.home.folderList

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaani.R
import com.vaani.models.Folder
import com.vaani.ui.folderMediaList.FolderMediaListFragment
import com.vaani.util.EmptyItemDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class FolderListFragment : Fragment(R.layout.list_layout) {

    private lateinit var refreshLayout: SwipeRefreshLayout
    private val foldersViewModel: FoldersViewModel by viewModels {
        FoldersViewModel.Factory(requireActivity().application)
    }
    private val job = Job()
    private val scope = CoroutineScope(job + Dispatchers.IO)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initChildViews(view)
    }

    private fun initChildViews(view: View) {

        val adapter = FolderAdapter(requireContext(), foldersViewModel.folderList.value ?: emptyList(), ::folderOnClick)

        val recyclerView: RecyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(EmptyItemDecoration())
        foldersViewModel.folderList.observe(viewLifecycleOwner, adapter::updateList)

        val fab: FloatingActionButton = view.findViewById(R.id.fab)
        fab.setOnClickListener {
            playMedia()
        }

        refreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        refreshLayout.setOnRefreshListener {
            scope.launch {
                foldersViewModel.refreshAllData()
                launch(Dispatchers.Main) {
                    refreshLayout.isRefreshing = false
                }
            }
        }
    }

    private fun playMedia() {
        TODO("Not yet implemented")
    }

    private fun folderOnClick(file: Folder) {
        requireParentFragment().parentFragmentManager.commit {
            add(R.id.fragment_container_view, FolderMediaListFragment(file))
            addToBackStack(null)
        }
    }

}