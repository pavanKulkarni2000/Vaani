package com.vaani.ui.listUtil

import com.vaani.models.UiItem

class Searcher<T : UiItem>(
  private val adapter: AbstractListAdapter<T>,
  private val displayList: MutableList<T>
) : ListAction(true) {
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
    adapter.notifyItemRangeRemoved(0, displayListBackup.size)
  }

  fun search(string: String?) {
    displayList.clear()
    string?.let { query ->
      if (query.length > 1) {
        displayList.addAll(searchItems.filter { it.name.contains(query) })
      }
    }
    adapter.notifyDataSetChanged()
  }

  fun closeSearch() {
    displayList.clear()
    displayList.addAll(displayListBackup)
    adapter.notifyDataSetChanged()
    searchItems.clear()
    displayListBackup.clear()
    searching = false
  }
}
