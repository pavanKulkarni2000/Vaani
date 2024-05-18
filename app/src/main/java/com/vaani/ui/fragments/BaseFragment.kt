package com.vaani.ui.fragments

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.search.SearchView
import com.vaani.R
import com.vaani.model.UiItem
import com.vaani.ui.adapter.Adapter
import com.vaani.ui.adapter.ItemClickProvider
import com.vaani.ui.util.EmptyItemDecoration
import java.util.concurrent.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.ensureActive

@UnstableApi
open class BaseFragment<T : UiItem>(layout: Int) :
  Fragment(layout),
  ItemClickProvider,
  SwipeRefreshLayout.OnRefreshListener,
  Toolbar.OnMenuItemClickListener {

  internal val localScope = CoroutineScope(Dispatchers.Default)
  internal val displayList = mutableListOf<T>()
  internal val adapter = Adapter(displayList, this)
  internal lateinit var recyclerView: RecyclerView
  internal lateinit var refreshLayout: SwipeRefreshLayout
  internal lateinit var fab: FloatingActionButton
  internal lateinit var toolbar: MaterialToolbar
  internal lateinit var searchView: SearchView
  internal lateinit var searchRecyclerView: RecyclerView
  open val data: List<T>
    get() = listOf()

  open val menuRes: Int = R.menu.empty_menu

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    resetData()
    recyclerView = view.findViewById(R.id.recycler_view)
    recyclerView.adapter = adapter
    recyclerView.addItemDecoration(EmptyItemDecoration())
    fab = view.findViewById(R.id.play_fab)
    fab.setOnClickListener(::fabAction)
    refreshLayout = view.findViewById(R.id.swipe_refresh_layout)
    refreshLayout.setOnRefreshListener(this)
    stopRefreshLayout()
    toolbar = view.findViewById(R.id.fragment_toolbar)
    toolbar.inflateMenu(menuRes)
    toolbar.setOnMenuItemClickListener(this)
    searchView = view.findViewById(R.id.fragment_searchview)
    localScope.ensureActive()
  }

  open fun fabAction(view: View?) {
    // Default implementation
  }

  override fun onRefresh() {
    // Default implementation
    stopRefreshLayout()
  }

  internal fun disableRefreshLayout() {
    refreshLayout.isEnabled = false
  }

  internal fun disableFab() {
    fab.hide()
  }

  internal fun stopRefreshLayout() {
    refreshLayout.isRefreshing = false
  }

  fun resetData(newList: List<T> = data) {
    displayList.clear()
    //default sort - sorted by name in asc by
    displayList.addAll(newList.sortedBy { it.name.lowercase() })
  }

  override fun onDestroy() {
    super.onDestroy()
    localScope.coroutineContext.cancelChildren(CancellationException("List View destroyed"))
  }

  override fun onItemClick(position: Int, view: View?) {
    // Default implementation
  }

  override fun onItemLongClick(position: Int, view: View?): Boolean {
    // Default implementation
    return false
  }

  override fun onMenuItemClick(item: MenuItem?): Boolean {
    return when (item?.itemId) {
      R.id.search -> {
        searchView.show()
        true
      }
      else -> false
    }
  }
}
