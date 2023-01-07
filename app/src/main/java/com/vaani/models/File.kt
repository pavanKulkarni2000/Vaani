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

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.BsonObjectId
import org.mongodb.kbson.ObjectId

class File(
    var name: String ,
    var isAudio: Boolean ,
    var path: String ,
    var isUri: Boolean ,
    var folderId: BsonObjectId
) : RealmObject {
    @PrimaryKey
    var id = ObjectId.invoke()
    private set

    constructor() : this("",false,"",false,ObjectId.invoke())

    override fun equals(other: Any?): Boolean = (this === other) || ((other as File).path == path)

    override fun hashCode(): Int = path.hashCode()
}