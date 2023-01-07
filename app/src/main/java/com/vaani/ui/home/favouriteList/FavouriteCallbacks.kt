package com.vaani.ui.home.favouriteList

import com.vaani.models.Favourite

interface FavouriteCallbacks {
    fun onClick(favourite: Favourite)
    fun onFavRemove(favourite: Favourite)
}