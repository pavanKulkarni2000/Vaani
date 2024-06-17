package com.vaani.dal

import com.vaani.db.DB
import com.vaani.db.entity.FavouriteEntity
import com.vaani.model.Favorite
import com.vaani.model.Media
import com.vaani.util.toFavourite

object Favorites {

    val all: List<Favorite>
        get() = DB.getFavourites().map(FavouriteEntity::toFavourite)
    fun addFavourite(media: Media): Favorite {
        val favourites = DB.isFavourite(media.id)
        if (favourites != null) {
            throw Exception("File already favorite")
        }
        val newFav = FavouriteEntity(0, DB.getFavouriteCount().toInt())
        newFav.media.targetId = media.id
        DB.save(newFav)
        return newFav.toFavourite()
    }

    fun remove(favEntity: Favorite) {
        DB.deleteFavourite(favEntity.id)
        val deletedRank = favEntity.rank
        val favourites = DB.getFavourites()
        for (fav in favourites) {
            if (fav.rank > deletedRank) {
                fav.rank--
            }
        }
        DB.saveFavourites(favourites)
    }

    fun move(rankFrom: Int, rankTo: Int) {
        val favourites = DB.getFavourites().sortedBy(FavouriteEntity::rank)
        if (rankFrom < rankTo) {
            for (i in rankFrom until rankTo) {
                favourites[i + 1].rank = i
            }
        } else {
            for (i in rankTo until rankFrom) {
                favourites[i].rank = i + 1
            }
        }
        favourites[rankFrom].rank = rankTo
        DB.saveFavourites(favourites)
    }

}