package com.vaani.ui.favouriteList

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.vaani.db.DB
import com.vaani.models.Favourite
import com.vaani.models.File
import com.vaani.util.Constants
import com.vaani.util.TAG
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class FavouriteViewModel(application: Application) : AndroidViewModel(application) {

    private var _favouriteMediaList = MutableLiveData(DB.CRUD.getFavourites())
    val favouriteMediaList: LiveData<List<Favourite>> = _favouriteMediaList

    fun addFavourite(favFile: File) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val newFav = DB.CRUD.upsertFavourite(Favourite().apply {
                    file.target = favFile
                    rank = _favouriteMediaList.value?.size ?: 0
                })
                launch(Dispatchers.Main) {
                    _favouriteMediaList.value = _favouriteMediaList.value!!.toMutableList().apply {  add(newFav) }
                    Log.d(TAG, "addFavourite: new fav list set")
                }
            } catch (e: Exception) {
                Log.e(TAG, "addFavourite: Fav creation error ",e)
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

    fun updateRank(from: Int, to: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val updated = DB.CRUD.updateFavourites(from,to)
            launch(Dispatchers.Main) {
                _favouriteMediaList.value = updated
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