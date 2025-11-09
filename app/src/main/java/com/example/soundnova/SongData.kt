package com.example.soundnova

data class SongData(
    val idSong: Long? = null,
    val idUser: String? = null,
    val title: String? = null,
    val artist: List<String> = emptyList(),
    val image: String? = null,
    val audioUrl: String? = null,
    var id: Int? = null
)