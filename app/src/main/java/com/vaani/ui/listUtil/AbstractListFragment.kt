package com.vaani.ui.listUtil

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import androidx.annotation.MenuRes
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.vaani.R
import com.vaani.models.UiItem
import com.vaani.ui.EmptyItemDecoration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import java.util.concurrent.CancellationException

@UnstableApi
abstract class AbstractListFragment<T : UiItem>(initialItems:List<T>) : Fragment(R.layout.list_fragment), MenuProvider {

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

  abstract fun fabAction(view: View)

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    recyclerView = view.findViewById(R.id.recycler_view)
    recyclerView.adapter = listAdapter
    recyclerView.addItemDecoration(EmptyItemDecoration())

    val fab: FloatingActionButton = view.findViewById(R.id.play_fab)
    fab.setOnClickListener(this::fabAction)

    refreshLayout = view.findViewById(R.id.swipe_refresh_layout)

    requireActivity().addMenuProvider(this)
  }

  fun resetData(newList: List<T>) {
    displayList.clear()
    displayList.addAll(newList)
    sorter.enabled = true
    sorter.sort(Sorter.SortOrder.ASC)
    searcher.enabled = true
    selector.enabled = true
  }

  override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
    menu.clear()
    menuInflater.inflate(generalMenu, menu)
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
    localScope.cancel(CancellationException("View destroyed"))
    requireActivity().removeMenuProvider(this)
  }
}
