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

package com.vaani.ui.home.folderList

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vaani.R
import com.vaani.models.Folder

class FolderAdapter(
    private var context: Context,
    private var Folders: List<Folder>,
    private val onClick: (Folder) -> Unit
) :
    RecyclerView.Adapter<FolderAdapter.FolderViewHolder>() {

    /* ViewHolder for Flower, takes in the inflated view and the onClick behavior. */
    inner class FolderViewHolder(itemView: View, val onClick: (Folder) -> Unit) :
        RecyclerView.ViewHolder(itemView) {
        private val folderText: TextView = itemView.findViewById(R.id.folder_text)
        private val folderSubtext: TextView =
            itemView.findViewById(R.id.folder_subtext)
        private var currentFolder: Folder? = null

        init {
            itemView.setOnClickListener {
                currentFolder?.let {
                    onClick(it)
                }
            }
        }

        /* Bind flower name and image. */
        fun bind(folder: Folder) {
            currentFolder = folder
            folderText.text = folder.name
            folderSubtext.text = context.getString(R.string.folder_subtext, folder.items)
        }
    }

    fun updateList(newList: List<Folder>) {
        Folders = newList
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
        val flower = Folders[position]
        holder.bind(flower)

    }

    override fun getItemCount(): Int = Folders.size
}