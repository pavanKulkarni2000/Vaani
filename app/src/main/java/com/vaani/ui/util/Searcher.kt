package com.vaani.ui.util

import android.util.Log
import com.vaani.data.model.UiItem
import com.vaani.util.TAG

class Searcher<T : UiItem>(private val displayList: MutableList<T>) {
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
    Log.d(TAG, "startSearch: started search")
  }

  fun search(string: String?) {
    displayList.clear()
    string?.let { query ->
      if (query.length > 1) {
        displayList.addAll(searchItems.filter { it.name.lowercase().contains(query.lowercase()) })
      }
    }
  }

  fun closeSearch() {
    Log.d(TAG, "closeSearch: closing the search, $displayListBackup")
    displayList.clear()
    displayList.addAll(displayListBackup)
    searchItems.clear()
    displayListBackup.clear()
    searching = false
    Log.d(TAG, "closeSearch: closed the search, $displayList")
  }
}
