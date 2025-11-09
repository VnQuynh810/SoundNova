package com.example.soundnova.data.local.dataSource

import com.example.soundnova.SongData
import com.example.soundnova.data.local.room.MusicDAO
import javax.inject.Inject

class RoomMusicDataSource @Inject constructor(private val musicDao: MusicDAO) {

    fun getAllSongsFlow(query: String = "") = musicDao.getAllSongsFlow(query)

    suspend fun getAllSongs() = musicDao.getAllSongs()

    fun getSongByGenre(genre: String) = musicDao.getSongByGenre(genre)

    suspend fun insertSong(songs: List<SongData>) = musicDao.insertSong(songs)

    suspend fun deleteAllSongs() = musicDao.deleteAllSongs()

    suspend fun deleteSong(song: SongData) = musicDao.deleteAllSongs(song)
    
}