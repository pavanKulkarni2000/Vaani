package com.vaani.models

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.annotation.Unique
import io.objectbox.relation.ToOne
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

@Entity
data class Favourite(
    @Id
    var id:Long=0,
    @Unique
    var rank: Int = 0,
){
    lateinit var file: ToOne<File>
}