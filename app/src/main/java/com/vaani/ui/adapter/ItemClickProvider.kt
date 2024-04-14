package com.vaani.ui.adapter

import android.view.View

interface ItemClickProvider {
  fun onItemClick(position: Int, view: View?)

  fun onItemLongClick(position: Int, view: View?): Boolean
}
