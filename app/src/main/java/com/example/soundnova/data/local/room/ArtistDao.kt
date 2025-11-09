package com.example.soundnova.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artists WHERE id = :artistId")
    suspend fun getArtistById(artistId: Long): ArtistEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity)
}
