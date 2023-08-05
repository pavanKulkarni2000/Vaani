package com.vaani.ui.files

import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.media3.common.util.UnstableApi
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.vaani.R
import com.vaani.data.Files
import com.vaani.models.FileEntity
import com.vaani.ui.medialist.MediaListFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@UnstableApi
class FileListFragment : MediaListFragment(Files.currentFolder) {
    private lateinit var copyLauncher: ActivityResultLauncher<Uri?>
    private lateinit var selectedFile: FileEntity
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        copyLauncher = registerForActivityResult(ActivityResultContracts.OpenDocumentTree(), this::copyFile)
    }

    override val refreshMediaList = SwipeRefreshLayout.OnRefreshListener {
        CoroutineScope(Job()).launch {
            Files.exploreFolder(Files.currentFolder)
            Files.setCurrentFolder(Files.currentFolder)
            launch(Dispatchers.Main) {
                mediaListAdapter.notifyDataSetChanged()
                refreshLayout.isRefreshing = false
            }
        }
    }

    override val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            val searchButton = menu.findItem(R.id.file_list_action_search)
            val searchView = searchButton?.actionView as SearchView
            searchView.setOnQueryTextListener(searchQuery)
            searchView.setOnCloseListener(searchClose)
        }

        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            return true
        }
    }

    override fun onOptions(position: Int, view: View) {
        val popup = PopupMenu(context!!, view)
        popup.menuInflater.inflate(R.menu.file_list_option_menu, popup.menu)
        selectedFile = Files.currentFiles[position]
        popup.menu.findItem(R.id.file_list_option_add_fav).setOnMenuItemClickListener {
            Files.addFavourite(selectedFile)
            true
        }
        popup.menu.findItem(R.id.file_list_option_copy).setOnMenuItemClickListener {
            copyLauncher.launch(null)
            true
        }
        popup.menu.findItem(R.id.file_list_option_move).setOnMenuItemClickListener {
            true
        }
        popup.menu.findItem(R.id.file_list_option_delete).setOnMenuItemClickListener {
            true
        }
        popup.show()
    }

    private fun copyFile(uri: Uri?) {
        refreshLayout.isRefreshing = true
        CoroutineScope(Job()).launch {
            Files.copyFile(selectedFile, uri, requireContext())
            launch(Dispatchers.Main) {
                refreshLayout.isRefreshing = false
            }
        }
    }
}