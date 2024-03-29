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
import com.vaani.ui.util.UiUtil
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Transient

@Entity
class MediaEntity(
  var isAudio: Boolean = false,
  var duration: Long = 0,
  var folderId: Long = 0,
  var playBackProgress: Float = 0F,
  var playBackSpeed: Float = 1F,
) : FileEntity() {
  override val subTitle: String
    get() = UiUtil.stringToTime(duration)

  override val image: Int
    get() =
      if(selected)
        R.drawable.check_circle_40px
      else
        when (isAudio) {
          true -> R.drawable.music_note_40px
          false -> R.drawable.movie_40px
        }
}
