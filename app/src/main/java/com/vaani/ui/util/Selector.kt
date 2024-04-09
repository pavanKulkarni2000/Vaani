package com.vaani.ui.util

import android.util.Log
import com.vaani.model.UiItem
import com.vaani.util.TAG
import java.util.LinkedList

class Selector<T : UiItem>(private val displayList: MutableList<T>) {

  val selection = LinkedList<Long>()

  var selecting = false
    private set

  val selectionCount: Int
    get() = selection.size

  fun selectId(id: Long) {
    val idx = displayList.indexOfFirst { it.id == id }
    if (idx < 0) {
      Log.d(TAG, "selectId: id not found")
    } else {
      selectAt(idx)
    }
  }

  fun selectAt(position: Int) {
    displayList[position].let {
      if (!selecting) {
        selecting = true
      }
      selection.add(it.id)
      it.selected = true
    }
  }

  fun isIdSelected(id: Long): Boolean = selection.contains(id)

  fun areIdsSelected(ids: Set<Long>): Boolean = selection.intersect(ids).isNotEmpty()

  fun unSelectId(id: Long) {
    val idx = displayList.indexOfFirst { it.id == id }
    if (idx < 0) {
      Log.d(TAG, "selectId: id not found")
    } else {
      unSelectAt(idx)
    }
  }

  fun unSelectAt(position: Int) {
    displayList[position].let { item ->
      if (selection.remove(item.id)) {
        if (selection.isEmpty()) {
          selecting = false
        }
      }
      item.selected = false
    }
  }
}
