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

package com.vaani.ui.medialist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import com.vaani.R
import com.vaani.models.FileEntity
import com.vaani.ui.UiUtil

@UnstableApi
class MediaListAdapter(
    private val files: List<FileEntity>,
    private val fileCallbacks: MediaItemCallbacks
) :
    RecyclerView.Adapter<MediaListAdapter.FileViewHolder>() {

    /* ViewHolder for Flower, takes in the inflated view and the onClick behavior. */
    inner class FileViewHolder(
        itemView: View
    ) :
        RecyclerView.ViewHolder(itemView) {
        private lateinit var file: FileEntity

        fun bind(position: Int) {
            file = files[position]
            itemView.setOnClickListener {
                fileCallbacks.onClick(file)
            }
            itemView.findViewById<TextView>(R.id.file_text).text = file.name
            itemView.findViewById<TextView>(R.id.file_subtext).text = UiUtil.stringToTime(file.duration)
            itemView.findViewById<ImageView>(R.id.file_image).setImageResource(
                when (file.isAudio) {
                    true -> R.drawable.foldermedia_music_note_40px
                    false -> R.drawable.foldermedia_movie_40px
                }
            )
            itemView.findViewById<ImageView>(R.id.options_icon).setOnClickListener {
                fileCallbacks.onOptions(position, it)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.file_item, parent, false)
        return FileViewHolder(view)
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int = files.size
}