package com.example.soundnova.data.local.room

import androidx.room.*
import com.example.soundnova.SongData
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDAO {

    @Query("SELECT * FROM music_table WHERE name LIKE '%' || :query || '%' OR artists LIKE '%' || :query || '%'")
    fun getAllSongsFlow(query: String): Flow<List<SongData>>

    @Query("SELECT * FROM music_table")
    suspend fun getAllSongs(): List<SongData>

    @Query("SELECT * FROM music_table WHERE genre = :genre")
    fun getSongByGenre(genre: String): Flow<List<SongData>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(songs: List<SongData>)

    @Query("DELETE FROM music_table")
    suspend fun deleteAllSongs()

    @Delete
    suspend fun deleteAllSongs(song: SongData)
}