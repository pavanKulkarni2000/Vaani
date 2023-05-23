/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this Folder except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.vaani.ui.folderList

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vaani.R
import com.vaani.models.FolderEntity

class FolderAdapter(
    private var context: Context,
    private var folderEntities: List<FolderEntity>,
    private val onClick: (FolderEntity) -> Unit
) :
    RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    /* ViewHolder for Flower, takes in the inflated view and the onClick behavior. */
    inner class FolderViewHolder(itemView: View, val onClick: (FolderEntity) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val folderText: TextView = itemView.findViewById(R.id.folder_text)
        private val folderSubtext: TextView =
            itemView.findViewById(R.id.folder_subtext)
        private var currentFolderEntity: FolderEntity? = null

        init {
            itemView.setOnClickListener {
                currentFolderEntity?.let {
                    onClick(it)
                }
            }
        }

        /* Bind flower name and image. */
        fun bind(folderEntity: FolderEntity) {
            currentFolderEntity = folderEntity
            folderText.text = folderEntity.name
            folderSubtext.text = context.getString(R.string.folder_subtext, folderEntity.items)
        }
    }

    fun updateList(newList: List<FolderEntity>) {
        folderEntities = newList
        notifyDataSetChanged()
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.folder_item, parent, false)
        return FolderViewHolder(view, onClick)
    }

    /* Gets current flower and uses it to bind view. */
    override fun onBindViewHolder(holder: FolderViewHolder, position: Int) {
        val flower = folderEntities[position]
        holder.bind(flower)

    }

    override fun getItemCount(): Int = folderEntities.size
}