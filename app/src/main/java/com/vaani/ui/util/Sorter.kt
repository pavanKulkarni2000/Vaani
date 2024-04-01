package com.vaani.ui.util

class Sorter<T : UiItem>(
  private val displayList: MutableList<T>
){
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
  }

  enum class SortOrder {
    RANK,
    ASC,
    DSC
  }
}
