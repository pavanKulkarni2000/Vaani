package com.vaani.ui.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vaani.R
import com.vaani.model.UiItem

class UiItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
  private val icon = view.findViewById<ImageView>(R.id.item_image)
  private val text = view.findViewById<TextView>(R.id.item_text)
  private val subtext = view.findViewById<TextView>(R.id.item_subtext)

  fun bind(item: UiItem) {
    text.text = item.name
    subtext.text = item.subTitle
    icon.setImageResource(item.image)
  }
  //
  //    fun selectView() {
  //      icon.setImageResource(R.drawable.check_circle_40px)
  //      view.isSelected = true
  //    }
  //
  //    fun unselectView(item: UiItem) {
  //      icon.setImageResource(item.image)
  //      view.isSelected = false
  //    }
  //
  //    fun bind(position: Int) {
  //      displayList[position].let { item ->
  //        var isSelected = selector.isSelected(item.id)
  //        view.findViewById<TextView>(R.id.item_text).text = item.name
  //        view.findViewById<TextView>(R.id.item_subtext).text = item.subTitle
  //        if (isSelected) {
  //          selectView()
  //        } else {
  //          unselectView(item)
  //        }
  //        view.setOnClickListener {
  //          if (!isSelected) {
  //            if (selector.selecting) {
  //              isSelected = true
  //              selectView()
  //              selector.select(item.id)
  //            } else {
  //              onItemClicked(position)
  //            }
  //          } else {
  //            isSelected = false
  //            unselectView(item)
  //            selector.unSelect(item.id)
  //          }
  //        }
  //        view.setOnLongClickListener {
  //          if (!isSelected) {
  //            isSelected = true
  //            selectView()
  //            selector.select(item.id)
  //          }
  //          true
  //        }
  //      }
  //    }
}
