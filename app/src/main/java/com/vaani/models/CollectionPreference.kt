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
    var single: Boolean,
    var loopAtEnd: Boolean
)
