package com.vaani.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vaani.db.entity.MediaEntity

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: MediaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(media: List<MediaEntity>)

    @Update
    suspend fun update(media: MediaEntity)

    @Query("UPDATE media SET playBackProgress = :progress WHERE id = :mediaId")
    suspend fun updateProgress(mediaId: Long, progress: Float)

    @Delete
    suspend fun delete(media: MediaEntity)
    
    @Query("DELETE FROM media WHERE id IN (:ids)")
    suspend fun delete(ids: List<Long>)

    @Query("SELECT * FROM media WHERE folderId = :folderId")
    suspend fun getMediaForFolder(folderId: Long): List<MediaEntity>

    @Query("SELECT * FROM media WHERE id = :mediaId")
    suspend fun getMedia(mediaId: Long): MediaEntity?

    @Query("SELECT * FROM media WHERE id IN (:mediaIds)")
    suspend fun getMedia(mediaIds: List<Long>): List<MediaEntity>
}
