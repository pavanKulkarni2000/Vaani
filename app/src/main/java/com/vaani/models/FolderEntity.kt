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

package com.vaani.models

import com.vaani.R
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Transient

@Entity
data class FolderEntity(
  var items: Int = 0,
  var playBackShuffle: Boolean = false,
  var lastPlayedId: Long = 0,
) : FileEntity() {
  override val subTitle: String
    get() = String.format("%d media files", items)

  override val image: Int
    get() =
      if(selected)
        R.drawable.check_circle_40px
      else
        R.drawable.folders_folder_48px

  override fun equals(other: Any?): Boolean {
    return super.equals(other)
  }

  override fun hashCode(): Int {
    return super.hashCode()
  }
}
