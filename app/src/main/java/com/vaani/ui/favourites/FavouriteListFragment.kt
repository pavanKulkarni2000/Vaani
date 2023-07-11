package com.vaani.ui.favourites

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.core.view.size
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.END
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.vaani.R
import com.vaani.data.Files
import com.vaani.models.FavSortOrder
import com.vaani.ui.medialist.MediaListFragment
import com.vaani.util.Constants.FAVOURITE_COLLECTION_ID
import com.vaani.util.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@UnstableApi
class FavouriteListFragment : MediaListFragment(PreferenceUtil.favouriteFolder) {

    override val refreshMediaList = SwipeRefreshLayout.OnRefreshListener {
        refreshLayout.isRefreshing = false
    }

    override val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.fav_list_action_menu,menu)
            val searchButton = menu.findItem(R.id.file_list_action_search)
            val searchView = searchButton?.actionView as SearchView
            searchView.setOnQueryTextListener(searchQuery)
            searchView.setOnCloseListener(searchClose)
            val sortButton = menu.findItem(R.id.fav_list_action_sort)
            sortButton.setIcon(when(Files.favSortOrder){
                FavSortOrder.RANK -> R.drawable.sort_by_alpha
                FavSortOrder.ASC,FavSortOrder.DSC -> R.drawable.sort_by_rank
            })
        }
        override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
            when(menuItem.itemId) {
                R.id.fav_list_action_sort -> {
                    when(Files.favSortOrder){
                        FavSortOrder.RANK -> {
                            Files.updateFavouriteSort(FavSortOrder.ASC)
                            menuItem.setIcon(R.drawable.sort_by_alpha)
                        }
                        FavSortOrder.ASC,FavSortOrder.DSC -> {
                            Files.updateFavouriteSort(FavSortOrder.RANK)
                            menuItem.setIcon(R.drawable.sort_by_rank)
                        }
                    }
                    mediaListAdapter.notifyDataSetChanged()
                }
            }
            return true
        }
    }

    override fun onOptions(position: Int, view: View) {
        val popup = PopupMenu(context!!, view)
        popup.menu.add(getString(R.string.favourite_remove_label)).apply {
            setIcon(R.drawable.foldermedia_favorite_24px)
            setOnMenuItemClickListener {
                Files.removeFavourite(position)
                mediaListAdapter.notifyItemRemoved(position)
                true
            }
        }
        popup.show()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ItemTouchHelper(touchHelper).attachToRecyclerView(recyclerView)
    }

    override fun onResume() {
        super.onResume()
        if (Files.favourites.size != recyclerView.size) {
            CoroutineScope(Dispatchers.Main).launch {
                mediaListAdapter.notifyItemRangeInserted(mediaListAdapter.itemCount, Files.favourites.size)
            }
        }
    }


    private val touchHelper = object:ItemTouchHelper.SimpleCallback(UP or DOWN, END) {
        override fun onMoved(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            fromPos: Int,
            target: RecyclerView.ViewHolder,
            toPos: Int,
            x: Int,
            y: Int
        ) {
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            val from = viewHolder.absoluteAdapterPosition
            val to = target.absoluteAdapterPosition
            Files.moveFavourite(from, to)
            mediaListAdapter.notifyItemMoved(from, to)
            return true
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            if (direction == END) {
                Files.removeFavourite(viewHolder.absoluteAdapterPosition)
                mediaListAdapter.notifyItemRemoved(viewHolder.absoluteAdapterPosition)
            }
        }
    }
}