package com.vaani.ui.util

import android.util.Log
import com.vaani.model.UiItem
import com.vaani.util.TAG

class Selector<T : UiItem>(
  private val displayList: MutableList<T>,
  private val selectionChangeListener: OnSelectionChangedListener,
) {

  val selection = mutableListOf<Long>()

  var selecting = false
    private set

  val selectionCount: Int
    get() = selection.size

  constructor(
    displayList: MutableList<T>
  ) : this(
    displayList,
    object : OnSelectionChangedListener {
      override fun selectingChanged(selecting: Boolean) {
        // empty
      }

      override fun selectionChanged(count: Int) {
        // empty
      }
    },
  )

  fun selectId(id: Long) {
    val idx = displayList.indexOfFirst { it.id == id }
    if (idx < 0) {
      Log.d(TAG, "selectId: id not found")
    } else {
      selectAt(idx)
    }
  }

  fun flipSelectionAt(position: Int) {
    displayList[position].let {
      if (isIdSelected(it.id)) {
        unSelectAt(position)
      } else {
        selectAt(position)
      }
    }
  }

  fun selectAt(position: Int) {
    displayList[position].let {
      if (!selecting) {
        selecting = true
        selectionChangeListener.selectingChanged(true)
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
          selectionChangeListener.selectingChanged(false)
        }
      }
      item.selected = false
    }
  }

  fun unselectAll(){
    if(selecting){
      displayList.forEach{it.selected=false}
      selection.clear()
      selecting=false
      selectionChangeListener.selectingChanged(false)
    }
  }

  interface OnSelectionChangedListener {
    fun selectingChanged(selecting: Boolean)
    fun selectionChanged(count: Int)
  }
}
