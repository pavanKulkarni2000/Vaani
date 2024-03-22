package com.vaani.list

import android.util.Log
import com.vaani.models.UiItem
import com.vaani.ui.common.MyAdapter
import com.vaani.util.TAG

class Searcher<T : UiItem>(private val displayList: MutableList<T>) : ListAction(true) {
  var adapter: MyAdapter<T>? = null
  private val displayListBackup = mutableListOf<T>()
  private val searchItems = mutableListOf<T>()
  var searching = false
    private set

  fun startSearch(items: List<T>) {
    searching = true
    searchItems.clear()
    searchItems.addAll(items)
    displayListBackup.clear()
    displayListBackup.addAll(displayList)
    displayList.clear()
    adapter?.notifyDataSetChanged()
    Log.d(TAG, "startSearch: started search")
  }

  fun search(string: String?) {
    displayList.clear()
    string?.let { query ->
      if (query.length > 1) {
        displayList.addAll(searchItems.filter { it.name.lowercase().contains(query.lowercase()) })
      }
    }
    adapter?.notifyDataSetChanged()
  }

  fun closeSearch() {
    displayList.clear()
    displayList.addAll(displayListBackup)
    adapter?.notifyDataSetChanged()
    searchItems.clear()
    displayListBackup.clear()
    searching = false
    Log.d(TAG, "closeSearch: closed the search")
  }
}
