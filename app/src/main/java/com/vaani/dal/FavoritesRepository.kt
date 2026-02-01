package com.vaani.dal

import android.util.Log
import com.vaani.db.FavouriteDao
import com.vaani.db.entity.FavouriteEntity
import com.vaani.model.Favorite
import com.vaani.util.TAG
import com.vaani.util.toFavorite
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class FavoritesRepository(private val favouriteDao: FavouriteDao) {

    fun getAll(): Flow<List<Favorite>> {
        return favouriteDao.getAllFavoritesData().map {
            it.map { data -> data.toFavorite() }
        }
    }

    suspend fun isFavourite(mediaId: Long): Boolean = withContext(Dispatchers.IO) {
        favouriteDao.isFavourite(mediaId)
    }

    suspend fun getFavouriteCount(): Long = withContext(Dispatchers.IO) {
        favouriteDao.getFavouriteCount()
    }

    suspend fun addFavorites(mediaIds: List<Long>): Int = withContext(Dispatchers.IO) {
        val newIds = mediaIds.toSet() - favouriteDao.getExistingFavoriteIds(mediaIds).toSet()

        if (newIds.isEmpty()) {
            return@withContext 0
        }

        var favRankStart = favouriteDao.getFavouriteCount().toInt()
        val favs = newIds.map {
            FavouriteEntity(rank = favRankStart++, mediaId = it)
        }
        favouriteDao.insertAll(favs)
        favs.size
    }

    suspend fun remove(favEntity: Favorite) = withContext(Dispatchers.IO) {
        favouriteDao.deleteAndReorder(favEntity.id, favEntity.rank)
        Log.d(TAG, "remove: $favEntity")
    }

    suspend fun move(fromRank: Int, toRank: Int) = withContext(Dispatchers.IO) {
        favouriteDao.moveRank(fromRank, toRank)
    }
}
