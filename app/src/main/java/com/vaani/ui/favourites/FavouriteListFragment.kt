package com.vaani.ui.favourites

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.PopupMenu
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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaani.R
import com.vaani.data.Files
import com.vaani.models.FavSortOrder
import com.vaani.ui.medialist.MediaListFragment
import com.vaani.util.ListUpdate
import com.vaani.util.PreferenceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@UnstableApi
class FavouriteListFragment : MediaListFragment(PreferenceUtil.favouriteFolder) {

    override val refreshMediaList = SwipeRefreshLayout.OnRefreshListener {
        refreshLayout.isRefreshing = false
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        ItemTouchHelper(touchHelper).attachToRecyclerView(recyclerView)

        val fab: FloatingActionButton = view.findViewById(R.id.sort_fab)
        fab.show()
        fab.setImageResource(if (Files.favSortOrder == FavSortOrder.ASC) R.drawable.sort_by_alpha else R.drawable.sort_by_rank)
        fab.setOnClickListener {
            when (Files.favSortOrder) {
                FavSortOrder.RANK -> {
                    Files.updateFavouriteSort(FavSortOrder.ASC)
                    fab.setImageResource(R.drawable.sort_by_alpha)
                }
                FavSortOrder.ASC, FavSortOrder.DSC -> {
                    Files.updateFavouriteSort(FavSortOrder.RANK)
                    fab.setImageResource(R.drawable.sort_by_rank)
                }
            }
            mediaListAdapter.notifyDataSetChanged()
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
        popup.menuInflater.inflate(R.menu.fav_list_option_menu, popup.menu)
        popup.menu.findItem(R.id.fav_list_option_del_fav).setOnMenuItemClickListener {
            Files.removeFavourite(position)
            mediaListAdapter.notifyItemRemoved(position)
            true
        }
        popup.show()
    }

    override fun onResume() {
        super.onResume()
        if (Files.favourites.size != recyclerView.size) {
            CoroutineScope(Dispatchers.Main).launch {
                mediaListAdapter.notifyItemRangeInserted(mediaListAdapter.itemCount, Files.favourites.size)
            }
        }
    }


    private val touchHelper = object : ItemTouchHelper.SimpleCallback(UP or DOWN, END) {
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