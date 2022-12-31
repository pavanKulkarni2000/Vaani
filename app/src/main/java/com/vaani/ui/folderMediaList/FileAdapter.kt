/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package com.vaani.ui.folderMediaList

import android.util.Log
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vaani.R
import com.vaani.models.File
import com.vaani.util.TAG
import kotlin.math.log

class FileAdapter(
    private var files: List<File>,
    private val fileCallbacks: FileCallbacks
) :
    RecyclerView.Adapter<FileAdapter.FileViewHolder>() {

    /* ViewHolder for Flower, takes in the inflated view and the onClick behavior. */
    inner class FileViewHolder(
        itemView: View, fileCallbacks: FileCallbacks
    ) :
        RecyclerView.ViewHolder(itemView) , View.OnCreateContextMenuListener{
        private val fileText: TextView = itemView.findViewById(R.id.file_text)
        private val fileIcon: ImageView = itemView.findViewById(R.id.file_image)
        private val optionsIcon : ImageView = itemView.findViewById(R.id.options_icon)
        private var currentFile: File? = null

        init {
            itemView.setOnClickListener {
                currentFile?.let {
                    fileCallbacks.onClick(it)
                }
            }
            optionsIcon.setOnClickListener {
                currentFile?.let { file->
                    fileCallbacks.onOptions(file, it)
                }
            }
        }

        /* Bind flower name and image. */
        fun bind(file: File) {
            currentFile = file
            fileText.text = file.name
            fileIcon.setImageResource(
                when (file.isAudio) {
                    true -> R.drawable.music_note_40px
                    false -> R.drawable.movie_40px
                }
            )
            itemView.setOnCreateContextMenuListener { contextMenu, _, _ ->
                contextMenu.add("Add to favourites").setOnMenuItemClickListener {
                    fileCallbacks.onFavourite(file)
                    true
                }
            }
        }

        override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
            menu?.add(Menu.NONE,v?.id?:0,Menu.NONE,"Favourite")
        }
    }

    fun updateList(newList: List<File>) {
        files = newList
        notifyDataSetChanged()
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.file_item, parent, false)
        return FileViewHolder(view, fileCallbacks)
    }

    /* Gets current flower and uses it to bind view. */
    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        val flower = files[position]
        holder.bind(flower)

    }

    override fun getItemCount(): Int = files.size
}