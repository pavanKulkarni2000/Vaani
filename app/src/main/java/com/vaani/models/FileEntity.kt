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

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique

@Entity
data class FileEntity(
    @Id
    var id: Long = 0,
    var name: String,
    var isAudio: Boolean,
    @Unique
    var path: String,
    var isUri: Boolean,
    var duration: Long,
    var folderId: Long,
    var playBackProgress: Float = 0F,
    var playBackSpeed: Float = 1F,
    var playBackLoop: Boolean = false
) {

    constructor() : this(0, "", false, "", false, 0, 0)

    override fun equals(other: Any?): Boolean = (this === other) || ((other as FileEntity).path == path)

    override fun hashCode(): Int = path.hashCode()

    fun copyPreferenceFrom(fileEntity: FileEntity) {
        this.playBackProgress = fileEntity.playBackProgress
        this.playBackSpeed = fileEntity.playBackSpeed
        this.playBackLoop = fileEntity.playBackLoop
    }
}