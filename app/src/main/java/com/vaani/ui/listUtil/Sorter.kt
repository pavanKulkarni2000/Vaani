package com.vaani.ui.listUtil

import com.vaani.models.UiItem

class Sorter<T : UiItem>(
  private val displayList: MutableList<T>,
  private val adapter: AbstractListAdapter<T>
) : ListAction(true) {
  var sortOrder: SortOrder = SortOrder.ASC

  fun sort(order: SortOrder) {
    sortOrder = order
    when (sortOrder) {
      SortOrder.RANK -> displayList.sortBy { it.rank }
      SortOrder.ASC -> displayList.sortBy { it.name.lowercase() }
      SortOrder.DSC -> {
        displayList.sortBy { it.name.lowercase() }
        displayList.reverse()
      }
    }
    adapter.notifyDataSetChanged()
  }

  enum class SortOrder {
    RANK,
    ASC,
    DSC
  }
}
