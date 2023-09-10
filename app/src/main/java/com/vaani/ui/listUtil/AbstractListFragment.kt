package com.vaani.ui.listUtil

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaani.R
import com.vaani.models.SortOrder
import com.vaani.ui.EmptyItemDecoration

@UnstableApi
abstract class AbstractListFragment<T> : Fragment(R.layout.list_layout), MenuProvider {

    protected lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    abstract val listAdapter: AbstractListAdapter
    protected var sortOrder: SortOrder = SortOrder.ASC
    val displayList: MutableList<T> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.adapter = listAdapter
        recyclerView.addItemDecoration(EmptyItemDecoration())

        val fab: FloatingActionButton = view.findViewById(R.id.play_fab)
        fab.setOnClickListener(this::fabAction)

        refreshLayout = view.findViewById(R.id.swipe_refresh_layout)
        refreshLayout.setOnRefreshListener(this::refreshAction)

        requireActivity().addMenuProvider(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().removeMenuProvider(this)
    }

    abstract fun refreshAction()
    fun refreshFinish() {
        refreshLayout.isRefreshing = false
    }

    abstract fun fabAction(view: View)
    abstract fun search(string: String?)
    abstract fun sort()
    fun resetData(newList: List<T>) {
        displayList.clear()
        displayList.addAll(newList)
        sort()
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        val searchButton = menu.findItem(R.id.list_action_search)
        val searchView = searchButton?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                search(query)
                listAdapter.notifyDataSetChanged()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                search(newText)
                listAdapter.notifyDataSetChanged()
                return true
            }
        })
        searchView.setOnCloseListener {
            search(null)
            listAdapter.notifyDataSetChanged()
            true
        }
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.list_action_sort_asc -> sortOrder = SortOrder.ASC
            R.id.list_action_sort_desc -> sortOrder = SortOrder.DSC
            R.id.list_action_sort_rank -> sortOrder = SortOrder.RANK
        }
        sort()
        listAdapter.notifyDataSetChanged()
        return true
    }
}