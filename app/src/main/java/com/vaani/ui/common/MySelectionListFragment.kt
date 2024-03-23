package com.vaani.ui.common

import android.os.Bundle
import android.service.controls.ControlsProviderService
import android.util.Log
import android.view.View
import androidx.media3.common.util.UnstableApi
import com.vaani.R
import com.vaani.list.Selector
import com.vaani.models.UiItem

@UnstableApi
abstract class MySelectionListFragment<T : UiItem>(initialItems: List<T>) : MyListFragment<T>(R.layout.selection_list_fragment,initialItems) {
    internal val selector = Selector(displayList)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        selector.adapter = listAdapter
    }

    override fun onItemLongClick(position: Int,view: View?): Boolean {
        Log.d(ControlsProviderService.TAG, "onLongClick: Not yet implemented")
        return false
    }
}