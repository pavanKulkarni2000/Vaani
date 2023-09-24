package com.vaani.ui.listUtil

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.annotation.MenuRes
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.RecyclerView

abstract class AbstractListAdapter(
    private val callbacks: ListItemCallbacks,
    @LayoutRes private val itemView: Int,
    @MenuRes private val optionsMenu: Int
) : RecyclerView.Adapter<AbstractListAdapter.ItemViewHolder>() {

    inner class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            setViewData(itemView, position)
            itemView.setOnClickListener {
                callbacks.onClick(position)
            }
            itemView.setOnLongClickListener {
                PopupMenu(it.context, it).apply {
                    menuInflater.inflate(optionsMenu, menu)
                    callbacks.onOptions(position, menu)
                    show()
                }
                true
            }
        }
    }

    abstract fun setViewData(view: View, position: Int)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(itemView, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(position)
    }

}