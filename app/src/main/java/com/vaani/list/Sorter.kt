package com.vaani.list

import com.vaani.models.UiItem
import com.vaani.ui.common.MyAdapter

class Sorter<T : UiItem>(
  private val displayList: MutableList<T>
) : ListAction(true) {
  var adapter: MyAdapter<T>? = null
  private var sortOrder: SortOrder = SortOrder.ASC

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
    adapter?.notifyDataSetChanged()
  }

  enum class SortOrder {
    RANK,
    ASC,
    DSC
  }
}
