package com.vaani.ui.home.favouriteList

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vaani.db.DB
import com.vaani.models.Favourite
import com.vaani.models.File
import com.vaani.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FavouriteViewModel(application: Application) : AndroidViewModel(application) {

    private var _favouriteMediaList = MutableLiveData(DB.CRUD.getFavourites())
    val favouriteMediaList: LiveData<List<Favourite>> = _favouriteMediaList

    fun addFavourite(favFile: File) {
        CoroutineScope(Dispatchers.IO).launch {
            launch(Dispatchers.Main) {
                _favouriteMediaList.value = mutableListOf<Favourite>().apply {
                    _favouriteMediaList.value?.let { addAll(it) }
                    add(DB.CRUD.upsertFavourite(Favourite(favFile).apply { rank = _favouriteMediaList.value?.size ?: 0 }))
                }
            }
        }
    }

    fun removeFavourite(favourite: Favourite) {
        CoroutineScope(Dispatchers.IO).launch {
            DB.CRUD.deleteFavourite(favourite)
            launch(Dispatchers.Main) {
                _favouriteMediaList.value = DB.CRUD.getFavourites()
            }
        }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            @Suppress("UNCHECKED_CAST")
            if (modelClass.isAssignableFrom(FavouriteViewModel::class.java)) {
                return FavouriteViewModel(application) as T
            }
            throw IllegalArgumentException(Constants.VIEWMODEL_FACTORY_ERROR)
        }
    }
}