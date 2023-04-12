package com.vaani.ui.folderMediaList

import android.view.View
import com.vaani.models.File

interface FileCallbacks {
    fun onClick(file: File)
    fun onOptions(file: File, view: View)
}