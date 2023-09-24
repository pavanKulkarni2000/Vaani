package com.vaani.ui.listUtil

import android.view.Menu

interface ListItemCallbacks {

    fun onClick(position: Int)
    fun onOptions(position: Int, menu: Menu)
}