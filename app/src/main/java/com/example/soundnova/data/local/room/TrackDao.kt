package com.example.soundnova.data.local.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks WHERE id = :trackId")
    suspend fun getTrackById(trackId: Long): TrackEntity?

    @Query("SELECT * FROM tracks WHERE artistId = :artistId")
    suspend fun getTracksByArtist(artistId: Long): List<TrackEntity>

    @Query("SELECT * FROM tracks WHERE albumId = :albumId")
    suspend fun getTracksByAlbum(albumId: Long): List<TrackEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)
}
