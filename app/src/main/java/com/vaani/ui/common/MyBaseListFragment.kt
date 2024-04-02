package com.vaani.ui.common

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaani.R
import com.vaani.data.model.UiItem
import com.vaani.ui.util.EmptyItemDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import java.util.concurrent.CancellationException

@UnstableApi
abstract class MyBaseListFragment<T : UiItem> :
  Fragment(R.layout.list_fragment), ItemClickProvider, SwipeRefreshLayout.OnRefreshListener {

  internal val localScope = CoroutineScope(Dispatchers.Default)
  internal lateinit var recyclerView: RecyclerView
  internal val displayList = mutableListOf<T>()
  internal val listAdapter = MyAdapter(displayList, this)
  internal lateinit var refreshLayout: SwipeRefreshLayout
  internal lateinit var fab: FloatingActionButton
  abstract val data: List<T>

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    resetData()
    recyclerView = view.findViewById(R.id.recycler_view)
    recyclerView.adapter = listAdapter
    recyclerView.addItemDecoration(EmptyItemDecoration())
    fab = view.findViewById(R.id.play_fab)
    fab.setOnClickListener(::fabAction)
    refreshLayout = view.findViewById(R.id.swipe_refresh_layout)
    refreshLayout.setOnRefreshListener(this)
    stopRefreshLayout()
  }

  abstract fun fabAction(view: View?)

  override fun onRefresh() {
    // Default implementation
    stopRefreshLayout()
  }

  internal fun disableRefreshLayout(){
    refreshLayout.isEnabled = false
  }

  internal fun disableFab(){
    fab.hide()
  }

  internal fun stopRefreshLayout() {
    refreshLayout.isRefreshing = false
  }

  fun resetData(newList: List<T> = data) {
    displayList.clear()
    displayList.addAll(newList)
  }

  override fun onDestroy() {
    super.onDestroy()
    localScope.cancel(CancellationException("List View destroyed"))
  }
}
