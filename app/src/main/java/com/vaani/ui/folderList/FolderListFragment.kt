package com.vaani.ui.folderList

import android.os.Bundle
import android.util.Log
import android.view.View
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
import com.vaani.ui.folderMediaList.FolderMediaListFragment
import com.vaani.ui.player.Player
import com.vaani.ui.player.VlcPlayerFragment
import com.vaani.util.Constants
import com.vaani.util.EmptyItemDecoration
import com.vaani.util.PreferenceUtil
import com.vaani.util.TAG
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
            onPlayClicked()
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

    private fun onPlayClicked() {
        var file: File? = null
        var folder: Folder? = null
        if(Player.mediaPlayerService.isPlaying && Player.mediaPlayerService.currentMediaFile?.folderId != Constants.FAVOURITE_COLLECTION_ID){
            file = Player.mediaPlayerService.currentMediaFile
            file?.let {
                file1->
                folder = foldersViewModel.folderList.value?.find { it.id == file1.folderId}
            }
        } else {
            folder = foldersViewModel.folderList.value?.find { it.id == PreferenceUtil.Folders.lastPlayedFolderId}
            folder?.let{
                folder1->
                val folderFiles = DB.CRUD.getFolderMediaList(folder1.id)
                DB.CRUD.getCollectionPreference(folder1.id)?.let {
                     collectionPref ->
                    file = folderFiles.find { it.id == collectionPref.lastPlayedId }
                }?:run{
                    file = folderFiles[0]
                }
            }
        }
            parentFragmentManager.commit {
                folder?.let {
                    add(R.id.fragment_container_view, FolderMediaListFragment(it))
                }
                file?.let {
                    add(R.id.fragment_container_view, VlcPlayerFragment(it))
                }
                addToBackStack(null)
            }
    }

    private fun folderOnClick(file: Folder) {
        requireParentFragment().parentFragmentManager.commit {
            add(R.id.fragment_container_view, FolderMediaListFragment(file))
            addToBackStack(null)
        }
    }

}