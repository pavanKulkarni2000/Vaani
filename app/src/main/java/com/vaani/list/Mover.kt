package com.vaani.list

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.vaani.models.UiItem
import com.vaani.ui.common.MyAdapter

abstract class Mover<T : UiItem>(
  displayList: MutableList<T>,
  adapter: MyAdapter<T>,
  recyclerView: RecyclerView
) : ListAction(true) {

  private val touchHelper =
    object : ItemTouchHelper.SimpleCallback(0, 0) {
      override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,
        target: RecyclerView.ViewHolder
      ): Boolean {
        val from = viewHolder.absoluteAdapterPosition
        val to = target.absoluteAdapterPosition
        move(from, to)
        val movedItem = displayList.removeAt(from)
        displayList.add(to, movedItem)
        adapter.notifyItemMoved(from, to)
        return true
      }

      override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        if (direction == ItemTouchHelper.END) {
          remove(viewHolder.absoluteAdapterPosition)
          displayList.removeAt(viewHolder.absoluteAdapterPosition)
          adapter.notifyItemRemoved(viewHolder.absoluteAdapterPosition)
        }
      }
    }

  init {
    ItemTouchHelper(touchHelper).attachToRecyclerView(recyclerView)
  }

  fun disableRemove() {
    touchHelper.setDefaultSwipeDirs(0)
  }

  fun disableMove() {
    touchHelper.setDefaultSwipeDirs(0)
  }

  fun enableRemove() {
    touchHelper.setDefaultSwipeDirs(ItemTouchHelper.LEFT)
  }

  fun enableMove() {
    touchHelper.setDefaultSwipeDirs(ItemTouchHelper.UP or ItemTouchHelper.DOWN)
  }

  abstract fun move(from: Int, to: Int)

  abstract fun remove(pos: Int)
}
