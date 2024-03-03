package com.vaani.ui.util.listUtil

abstract class Selector : ListAction(true) {

  val selection = mutableSetOf<Long>()

  var selecting = false
    private set

  val selectionCount: Int
    get() = selection.size

  fun select(id: Long) {
    if (!selecting) {
      selecting = true
      onSelectingChanged()
    }
    selection.add(id)
    onSelectItemsChanged()
  }

  fun isSelected(id: Long): Boolean = selection.contains(id)

  fun areSelected(ids: Set<Long>): Boolean = selection.intersect(ids).isNotEmpty()

  fun unSelect(id: Long) {
    if (selection.remove(id)) {
      if (selection.isEmpty()) {
        selecting = false
        onSelectingChanged()
      }
      onSelectItemsChanged()
    }
  }

  fun unSelect(ids: Set<Long>) {
    if (selection.removeAll(ids)) {
      if (selection.isEmpty()) {
        selecting = false
        onSelectingChanged()
      }
      onSelectItemsChanged()
    }
  }

  abstract fun onSelectingChanged()

  abstract fun onSelectItemsChanged()
}
