package com.vaani.ui.common

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.vaani.R
import com.vaani.models.UiItem

class MyAdapter<T : UiItem>(
  private val displayList: MutableList<T>,private val clickProvider: ItemClickProvider
) : RecyclerView.Adapter<UiItemViewHolder>() {

  override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UiItemViewHolder {
    val view = LayoutInflater.from(parent.context).inflate(R.layout.list_item_layout, parent, false)
    return UiItemViewHolder(view)
  }

  override fun getItemCount(): Int = displayList.size

  override fun onBindViewHolder(holder: UiItemViewHolder, position: Int) {
    displayList[position].let {
      holder.bind(it)
      holder.itemView.setOnClickListener{clickProvider.onItemClick(position,it)}
      holder.itemView.setOnLongClickListener{clickProvider.onItemLongClick(position,it)}
    }
  }
}
