package com.vaani.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.vaani.db.entity.FavouriteEntity
import com.vaani.model.FavoriteData
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favourite: FavouriteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(favourites: List<FavouriteEntity>)

    @Delete
    suspend fun delete(favourite: FavouriteEntity)

    @Query("SELECT * FROM favourites")
    suspend fun getFavourites(): List<FavouriteEntity>

    @Query("SELECT mediaId FROM favourites WHERE mediaId IN (:mediaIds)")
    suspend fun getExistingFavoriteIds(mediaIds: List<Long>): List<Long>

    @Query("""
        SELECT
            f.id AS id,
            m.id AS fileId,
            m.name AS name,
            f.rank AS rank,
            m.isAudio AS isAudio,
            m.duration AS duration
        FROM
            favourites f
        JOIN
            media m ON f.mediaId = m.id
        ORDER BY
            f.rank
    """)
    fun getAllFavoritesData(): Flow<List<FavoriteData>>

    @Query("SELECT EXISTS(SELECT 1 FROM favourites WHERE mediaId = :mediaId)")
    suspend fun isFavourite(mediaId: Long): Boolean

    @Query("SELECT COUNT(*) FROM favourites")
    suspend fun getFavouriteCount(): Long

    @Query("DELETE FROM favourites WHERE id = :favouriteId")
    suspend fun deleteFavourite(favouriteId: Long)

    @Query("UPDATE favourites SET rank = rank - 1 WHERE rank > :deletedRank")
    suspend fun decrementRanksAfter(deletedRank: Int)

    @Transaction
    suspend fun deleteAndReorder(favouriteId: Long, deletedRank: Int) {
        deleteFavourite(favouriteId)
        decrementRanksAfter(deletedRank)
    }

    @Query("UPDATE favourites SET rank = :newRank WHERE rank = :oldRank")
    suspend fun updateRank(oldRank: Int, newRank: Int)

    @Query("UPDATE favourites SET rank = rank + 1 WHERE rank >= :toRank AND rank < :fromRank")
    suspend fun incrementRanksForMove(fromRank: Int, toRank: Int)

    @Query("UPDATE favourites SET rank = rank - 1 WHERE rank > :fromRank AND rank <= :toRank")
    suspend fun decrementRanksForMove(fromRank: Int, toRank: Int)

    @Transaction
    suspend fun moveRank(fromRank: Int, toRank: Int) {
        if (fromRank == toRank) return
        // Using a temporary rank that is guaranteed not to exist.
        val tempRank = -1

        // 1. Isolate the item being moved.
        updateRank(fromRank, tempRank)

        // 2. Shift the ranks of other items in the affected range.
        if (fromRank < toRank) {
            // Moving an item down the list (e.g., from rank 3 to 5)
            decrementRanksForMove(fromRank, toRank)
        } else {
            // Moving an item up the list (e.g., from rank 5 to 3)
            incrementRanksForMove(fromRank, toRank)
        }

        // 3. Set the final rank of the moved item.
        updateRank(tempRank, toRank)
    }
}
