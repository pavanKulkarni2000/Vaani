package com.vaani.ui.common

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.MenuItem.OnActionExpandListener
import android.view.View
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.vaani.R
import com.vaani.models.UiItem
import com.vaani.ui.MainActivity
import com.vaani.ui.util.EmptyItemDecoration
import com.vaani.list.Searcher
import com.vaani.list.Sorter

@UnstableApi
abstract class MyListFragment<T : UiItem>(fragmentLayout: Int, initialItems: List<T>) :
  Fragment(fragmentLayout), MenuCreator, ItemClickProvider {

  public @get:IdRes abstract val menuGroup: Int
  public abstract val subtitle: String
  internal val displayList = initialItems.toMutableList()
  internal val listAdapter = MyAdapter(displayList ,this)
  internal val sorter = Sorter(displayList)
  internal val searcher = Searcher(displayList)
  internal lateinit var recyclerView: RecyclerView

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)

    recyclerView = view.findViewById(R.id.recycler_view)
    recyclerView.adapter = listAdapter
    recyclerView.addItemDecoration(EmptyItemDecoration())

    sorter.adapter = listAdapter
    searcher.adapter = listAdapter
  }

  override fun onPause() {
    super.onPause()
    MainActivity.menuGroupActiveMap[menuGroup] = false
  }

  override fun onResume() {
    super.onResume()
    MainActivity.menuGroupActiveMap[menuGroup] = true
    requireActivity().let {
      it.invalidateMenu()
      (it as AppCompatActivity).supportActionBar?.subtitle = subtitle
    }
  }

  override fun createMenu(menu: Menu) {
    for (item in menu.children) {
      when (item.itemId) {
        R.id.search -> createSearch(item)
        R.id.sort -> createSort(item)
      }
    }
  }

  private fun createSearch(item: MenuItem) {
    val searchView = item.actionView as SearchView
    searchView.setOnQueryTextListener(
      object : OnQueryTextListener {
        override fun onQueryTextSubmit(query: String?): Boolean {
          searcher.search(query)
          return true
        }

        override fun onQueryTextChange(newText: String?): Boolean {
          searcher.search(newText)
          return true
        }
      }
    )
    item.setOnActionExpandListener(
      object : OnActionExpandListener {
        override fun onMenuItemActionExpand(item: MenuItem): Boolean {
          searcher.startSearch(displayList)
          return true
        }

        override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
          searcher.closeSearch()
          return true
        }
      }
    )
  }

  private fun createSort(menuItem: MenuItem) {
    menuItem.subMenu?.let {
      for (sortType in it.children) {
        when (sortType.itemId) {
          R.id.sort_asc -> sorter.sort(Sorter.SortOrder.ASC)
          R.id.sort_desc -> sorter.sort(Sorter.SortOrder.DSC)
          R.id.sort_rank -> sorter.sort(Sorter.SortOrder.RANK)
        }
      }
    }
  }

  fun resetData(newList: List<T>) {
    displayList.clear()
    displayList.addAll(newList)
  }

}
