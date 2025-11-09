package com.example.soundnova.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums WHERE id = :albumId")
    suspend fun getAlbumById(albumId: Long): AlbumEntity?

    @Query("SELECT * FROM albums WHERE artistId = :artistId")
    suspend fun getAlbumsByArtist(artistId: Long): List<AlbumEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)
}
