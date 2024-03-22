package com.vaani.list

import android.util.Log
import com.vaani.models.UiItem
import com.vaani.ui.common.MyAdapter
import com.vaani.util.TAG

class Selector<T : UiItem>(private val displayList: MutableList<T>) : ListAction(true) {
  var adapter: MyAdapter<T>? = null

  val selection = mutableSetOf<Long>()

  var selecting = false
    private set

  val selectionCount: Int
    get() = selection.size

  fun selectId(id: Long) {
    val idx = displayList.indexOfFirst { it.id == id }
    if (idx < 0) {
      Log.d(TAG, "selectId: id not found")
    }else{
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
      adapter?.notifyItemChanged(position)
    }
  }

  fun isIdSelected(id: Long): Boolean = selection.contains(id)

  fun areIdsSelected(ids: Set<Long>): Boolean = selection.intersect(ids).isNotEmpty()

  fun unSelectId(id: Long) {
    val idx = displayList.indexOfFirst { it.id == id }
    if (idx < 0) {
      Log.d(TAG, "selectId: id not found")
    }else{
      unSelectAt(idx)
    }
  }
  fun unSelectAt(position: Int) {
    displayList[position].let {
      item->
      if (selection.remove(item.id)) {
        if (selection.isEmpty()) {
          selecting = false
        }
      }
      item.selected = false
      adapter?.notifyItemChanged(position)
    }
  }

}
