package com.vaani.ui.files

import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.media3.common.util.UnstableApi
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.vaani.R
import com.vaani.data.Files
import com.vaani.ui.medialist.MediaListFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

@UnstableApi
class FileListFragment : MediaListFragment(Files.currentFolder) {

    override val refreshMediaList = SwipeRefreshLayout.OnRefreshListener {
        CoroutineScope(Job()).launch {
            Files.exploreFolder(Files.currentFolder)
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
        popup.menu.add(getString(R.string.favourite_add_label)).apply {
            setIcon(R.drawable.foldermedia_favorite_filled_24px)
            setOnMenuItemClickListener {
                Files.addFavourite(Files.currentFiles[position])
                true
            }
        }
        popup.show()
    }

}