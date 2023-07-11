package com.vaani.ui.medialist

import android.view.View
import com.vaani.models.FileEntity

interface MediaItemCallbacks {

    fun onClick(file: FileEntity)
    fun onOptions(position: Int, view: View)

}