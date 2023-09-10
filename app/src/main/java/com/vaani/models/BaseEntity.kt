package com.vaani.models

import io.objectbox.annotation.BaseEntity
import io.objectbox.annotation.Id

@BaseEntity
open class BaseEntity(
    @Id
    var id: Long = 0
)