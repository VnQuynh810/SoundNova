package com.example.soundnova.repository

import com.example.soundnova.data.local.room.MusicDatabase
import com.example.soundnova.data.local.room.TrackEntity
import com.example.soundnova.models.TrackData


class MusicRepository(private val musicDatabase: MusicDatabase) {

    private val trackDao = musicDatabase.trackDao()

    suspend fun saveTrackData(track: TrackData) {
        val trackEntity = TrackEntity(
            id = track.id!!,
            title = track.title!!,
            artistId = track.artist!!.id!!,
            artistName = track.artist!!.name!!,
            albumId = track.album!!.id!!,
            albumTitle = track.album!!.title!!,
            image = track.artist!!.pictureBig!!,
            preview = track.preview!!,
        )
        trackDao.insertTracks(listOf(trackEntity))
    }
}