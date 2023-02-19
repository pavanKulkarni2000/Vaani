package com.vaani.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique

@Entity
data class CollectionPreference(
    @Id
    var id: Long,
    @Unique
    var collectionId: Long,
    var shuffle: Boolean,
    var lastPlayedId: Long
)
{
    constructor() : this(0,0,false,0)
}
