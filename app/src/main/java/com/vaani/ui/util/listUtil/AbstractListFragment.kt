package com.vaani.ui.util.listUtil

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.MenuRes
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaani.ui.MainActivity
import com.vaani.R
import com.vaani.models.UiItem
import com.vaani.ui.util.EmptyItemDecoration
import com.vaani.util.TAG
import java.util.concurrent.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel

@UnstableApi
abstract class AbstractListFragment<T : UiItem>(initialItems: List<T>) :
  Fragment(R.layout.list_fragment) {

  val displayList: MutableList<T> = initialItems.toMutableList()
  internal val selector =
    object : Selector() {
      override fun onSelectingChanged() = this@AbstractListFragment.onSelectingChanged(selecting)

      override fun onSelectItemsChanged() =
        this@AbstractListFragment.onSelectItemsChanged(selection)
    }
  internal val listAdapter =
    object : AbstractListAdapter<T>(displayList, selector) {
      override fun onItemClicked(position: Int) = this@AbstractListFragment.onItemClicked(position)
    }
  internal val sorter = Sorter(displayList, listAdapter)
  internal val searcher = Searcher(listAdapter, displayList)
  internal lateinit var recyclerView: RecyclerView
  internal lateinit var refreshLayout: SwipeRefreshLayout
  internal val localScope = CoroutineScope(Dispatchers.Default)
  @get:MenuRes abstract val generalMenu: Int
  @get:MenuRes abstract val selectedMenu: Int
  abstract var subtitle: String

  abstract fun fabAction(view: View)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    recyclerView = view.findViewById(R.id.recycler_view)
    recyclerView.adapter = listAdapter
    recyclerView.addItemDecoration(EmptyItemDecoration())

    val fab: FloatingActionButton = view.findViewById(R.id.play_fab)
    fab.setOnClickListener(this::fabAction)

    refreshLayout = view.findViewById(R.id.swipe_refresh_layout)
  }

  override fun onPause() {
    super.onPause()
    Log.d(TAG, "onPause: paused")
  }

  override fun onResume() {
    super.onResume()
    MainActivity.optionsMenu = if (selector.selecting) selectedMenu else generalMenu
    requireActivity().let {
      it.invalidateMenu()
      (it as AppCompatActivity).supportActionBar?.subtitle = subtitle
    }
  }

  fun resetData(newList: List<T>) {
    displayList.clear()
    displayList.addAll(newList)
    sorter.enabled = true
    sorter.sort(Sorter.SortOrder.ASC)
    searcher.enabled = true
    selector.enabled = true
  }

  abstract fun onItemClicked(position: Int)

  fun onSelectItemsChanged(selection: MutableSet<Long>) {
    if (selection.count() > 1) {}
  }

  private fun onSelectingChanged(selecting: Boolean) {
    if (selecting) {} else {}
  }

  override fun onDestroy() {
    super.onDestroy()
    localScope.cancel(CancellationException("List View destroyed"))
  }
}
