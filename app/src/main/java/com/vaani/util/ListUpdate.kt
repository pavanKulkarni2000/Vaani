package com.vaani.util

data class ListUpdate(
    val type : Type,
    val x: Int,
    val y:Int
){
    enum class Type{ UPDATE_ALL,INSERT_AT,INSERT_RANGE }
}