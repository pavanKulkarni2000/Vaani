package com.vaani.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.vaani.db.entity.FavouriteEntity
import com.vaani.db.entity.FolderEntity
import com.vaani.db.entity.MediaEntity
import com.vaani.model.Favorite
import com.vaani.util.toFavorite
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class FavoritesDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var folderDao: FolderDao
    private lateinit var mediaDao: MediaDao
    private lateinit var favouriteDao: FavouriteDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        folderDao = db.folderDao()
        mediaDao = db.mediaDao()
        favouriteDao = db.favouriteDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun getAllFavoritesData_returnsCorrectData() = runBlocking {
        // Given
        val folder = FolderEntity(id = 1, name = "test", path = "/test", isUri = false)
        folderDao.insert(folder)

        val media1 = MediaEntity(id = 1, name = "media1", path = "/test/media1", isUri = false, isAudio = true, duration = 100, playBackProgress = 0f, folderId = 1)
        val media2 = MediaEntity(id = 2, name = "media2", path = "/test/media2", isUri = false, isAudio = true, duration = 200, playBackProgress = 0f, folderId = 1)
        mediaDao.insertAll(listOf(media1, media2))

        val fav1 = FavouriteEntity(id = 1, rank = 0, mediaId = 1)
        val fav2 = FavouriteEntity(id = 2, rank = 1, mediaId = 2)
        favouriteDao.insertAll(listOf(fav1, fav2))

        // When
        val favorites: List<Favorite> = favouriteDao.getAllFavoritesData().map { it.toFavorite() }

        // Then
        assertEquals(2, favorites.size)

        val favData1 = favorites[0]
        assertEquals(fav1.id, favData1.id)
        assertEquals(media1.id, favData1.fileId)
        assertEquals(media1.name, favData1.name)
        assertEquals(fav1.rank, favData1.rank)

        val favData2 = favorites[1]
        assertEquals(fav2.id, favData2.id)
        assertEquals(media2.id, favData2.fileId)
        assertEquals(media2.name, favData2.name)
        assertEquals(fav2.rank, favData2.rank)
    }
}
