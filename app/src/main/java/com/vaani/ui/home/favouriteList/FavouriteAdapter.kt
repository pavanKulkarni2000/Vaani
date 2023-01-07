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

package com.vaani.ui.home.favouriteList

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.vaani.R
import com.vaani.models.Favourite
import com.vaani.util.TAG

class FavouriteAdapter(
    private var files: List<Favourite>,
    private val favouriteCallbacks: FavouriteCallbacks
) :
    RecyclerView.Adapter<FavouriteAdapter.FavouriteViewHolder>() {

    /* ViewHolder for Flower, takes in the inflated view and the onClick behavior. */
    inner class FavouriteViewHolder(
        itemView: View, private val favouriteCallbacks: FavouriteCallbacks
    ) :
        RecyclerView.ViewHolder(itemView) {
        private val fileText: TextView = itemView.findViewById(R.id.file_text)
        private val fileIcon: ImageView = itemView.findViewById(R.id.file_image)
        private var currentFile: Favourite? = null

        init {
            itemView.setOnClickListener {
                currentFile?.let {
                    favouriteCallbacks.onClick(it)
                }
            }
        }

        /* Bind flower name and image. */
        fun bind(favourite: Favourite) {
            currentFile = favourite
            fileText.text = favourite.file!!.name
            fileIcon.setImageResource(
                when (favourite.file!!.isAudio) {
                    true -> R.drawable.foldermedia_music_note_40px
                    false -> R.drawable.foldermedia_movie_40px
                }
            )
        }
    }

    fun updateList(newList: List<Favourite>) {
        Log.d(TAG, "updateList: update")
        files = newList
        notifyDataSetChanged()
    }

    /* Creates and inflates view and return FlowerViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.file_item, parent, false)
        return FavouriteViewHolder(view, favouriteCallbacks)
    }

    /* Gets current flower and uses it to bind view. */
    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        val flower = files[position]
        holder.bind(flower)

    }

    override fun getItemCount(): Int = files.size
}