package com.vaani.ui.files

import android.view.View
import com.vaani.models.FileEntity

interface FileCallbacks {
    fun onClick(file: FileEntity)
    fun onOptions(file: FileEntity, view: View)
}