package com.vaani.models

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Favourite(
    var file: File? = null
) : RealmObject {
    @PrimaryKey
    var rank: Int = 0
    constructor() : this(File())
}