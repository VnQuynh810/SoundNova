package com.example.soundnova.data.local.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tracks")
data class TrackEntity(
    @PrimaryKey val id: Long,
    val title: String,
    val artistId: Long,
    val artistName: String,
    val albumId: Long,
    val albumTitle: String,
    val image: String,
    val preview: String,
)