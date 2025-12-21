package com.vaani.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.vaani.db.entity.FolderEntity

@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(folder: FolderEntity): Long

    @Update
    suspend fun update(folder: FolderEntity)

    @Query("UPDATE folders SET lastPlayedMediaId = :lastPlayedId WHERE id = :folderId")
    suspend fun updateLastPlayedId(folderId: Long, lastPlayedId: Long)

    @Delete
    suspend fun delete(folder: FolderEntity)

    @Query("SELECT * FROM folders")
    suspend fun getAll(): List<FolderEntity>

    @Query("SELECT * FROM folders WHERE id = :folderId")
    suspend fun getFolder(folderId: Long): FolderEntity?

    @Query("SELECT * FROM folders WHERE path = :path")
    suspend fun getFolderByPath(path: String): FolderEntity?

    @Query("SELECT * FROM folders WHERE name LIKE '%' || :query || '%' ")
    suspend fun searchFolders(query: String): List<FolderEntity>
}
