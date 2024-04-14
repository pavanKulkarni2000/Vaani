package com.vaani.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaani.R
import com.vaani.model.UiItem
import com.vaani.ui.adapter.Adapter
import com.vaani.ui.adapter.ItemClickProvider
import com.vaani.ui.util.EmptyItemDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.ensureActive
import java.util.concurrent.CancellationException

@UnstableApi
abstract class BaseFragment<T : UiItem> :
  Fragment(), ItemClickProvider, SwipeRefreshLayout.OnRefreshListener {

  internal val localScope = CoroutineScope(Dispatchers.Default)
  internal lateinit var recyclerView: RecyclerView
  internal val displayList = mutableListOf<T>()
  internal val adapter = Adapter(displayList, this)
  internal lateinit var refreshLayout: SwipeRefreshLayout
  internal lateinit var fab: FloatingActionButton
  abstract val data: List<T>
  abstract val fragmentRes: Int

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
     super.onCreateView(inflater, container, savedInstanceState)
    return inflater.inflate(fragmentRes,container)
  }
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
    displayList.addAll(newList)
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
}
