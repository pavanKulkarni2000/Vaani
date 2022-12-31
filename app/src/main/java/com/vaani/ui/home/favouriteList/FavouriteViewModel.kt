package com.vaani.ui.home.favouriteList

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vaani.db.DB
import com.vaani.models.Favourite
import com.vaani.models.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavouriteViewModel : ViewModel() {

    private var _favouriteMediaList = MutableLiveData(DB.getFavourites())
    val favouriteMediaList: LiveData<List<File>> = _favouriteMediaList

    fun addFavourite(favFile: File) {
        CoroutineScope(Dispatchers.IO).launch {
            DB.updateFavourite(Favourite(0,favFile.id,favouriteMediaList.value!!.size))
            launch(Dispatchers.Main) {
                _favouriteMediaList.value = DB.getFavourites()
            }
        }
    }
    fun removeFavourite(favFile: File) {
        CoroutineScope(Dispatchers.IO).launch {
            DB.deleteFavourite(Favourite(0,favFile.id,favouriteMediaList.value!!.size))
            launch(Dispatchers.Main) {
                _favouriteMediaList.value = DB.getFavourites()
            }
        }
    }
}