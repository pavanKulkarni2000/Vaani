package com.vaani.ui.player

interface VideoGestureListener {

    fun onSingleTap()

    fun onHorizontalScroll(seekForward: Boolean)

    fun onVerticalScroll(percent: Float, direction: Int)

    fun onDoubleTap()
}