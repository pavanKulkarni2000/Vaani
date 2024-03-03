package com.vaani.ui.util.listUtil

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vaani.R
import com.vaani.models.UiItem

abstract class AbstractListAdapter<T : UiItem>(
  private val displayList: MutableList<T>,
  private val selector: Selector
) : RecyclerView.Adapter<AbstractListAdapter<T>.ItemViewHolder>() {

  inner class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
    private fun selectView() {
      view.findViewById<ImageView>(R.id.item_image).setImageResource(R.drawable.check_circle_40px)
      view.isSelected = true
    }

    private fun unselectView(item: UiItem) {
      view.findViewById<ImageView>(R.id.item_image).setImageResource(item.image)
      view.isSelected = false
    }

    fun bind(position: Int) {
      displayList[position].let { item ->
        var isSelected = selector.isSelected(item.id)
        view.findViewById<TextView>(R.id.item_text).text = item.name
        view.findViewById<TextView>(R.id.item_subtext).text = item.subTitle
        if (isSelected) {
          selectView()
        } else {
          unselectView(item)
        }
        view.setOnClickListener {
          if (!isSelected) {
            if (selector.selecting) {
              isSelected = true
              selectView()
              selector.select(item.id)
            } else {
              onItemClicked(position)
            }
          } else {
            isSelected = false
            unselectView(item)
            selector.unSelect(item.id)
          }
        }
        view.setOnLongClickListener {
          if (!isSelected) {
            isSelected = true
            selectView()
            selector.select(item.id)
          }
          true
        }
      }
    }
  }

  abstract fun onItemClicked(position: Int)

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_layout, parent, false)
    return ItemViewHolder(view)
  }

  override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
    holder.bind(position)
  }

  override fun getItemCount(): Int = displayList.size
}
