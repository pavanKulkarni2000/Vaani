package com.vaani.ui.fragments

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import com.vaani.dal.FavoritesRepository
import com.vaani.dal.FileManager
import com.vaani.model.Favorite
import com.vaani.model.Media

class FavoriteViewModel(
    favoritesRepository: FavoritesRepository,
    private val fileManager: FileManager
) : ViewModel() {

    val favorites: LiveData<List<Favorite>> = favoritesRepository.getAll().asLiveData()

    suspend fun getMedias(favorites: List<Favorite>): List<Media> {
        val mediaIds = favorites.map { it.fileId }
        return fileManager.getMedias(mediaIds)
    }
}

class FavoriteViewModelFactory(
    private val favoritesRepository: FavoritesRepository,
    private val fileManager: FileManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FavoriteViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FavoriteViewModel(favoritesRepository, fileManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
