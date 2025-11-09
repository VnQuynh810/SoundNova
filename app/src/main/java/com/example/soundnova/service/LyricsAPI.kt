package com.example.soundnova.service

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface LyricsApi {

    @GET("v1/{artist}/{title}")
    suspend fun getLyrics(
        @Path("artist") artist: String,
        @Path("title") title: String
    ): Response<LyricsResponse>
}

@Parcelize
data class LyricsResponse(
    @SerializedName("lyrics")
    val lyrics: String
): Parcelable

object LyricsApiHelper {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.lyrics.ovh/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(LyricsApi::class.java)

    @Throws(RuntimeException::class)
    suspend fun fetchLyrics(artist: String, title: String): String {
        val response = api.getLyrics(artist, title)
        if (response.isSuccessful) {
            return response.body()?.lyrics ?: throw RuntimeException("No lyrics found")
        } else {
            throw RuntimeException("API call failed: ${response.errorBody()?.string()}")
        }
    }
}

suspend fun main() {
    try {
        val artist = "Black Pink"
        val title = "Kill This Love"

        val lyrics = LyricsApiHelper.fetchLyrics(artist, title)
        println("Lyrics for '$title' by '$artist':\n$lyrics")

    } catch (e: Exception) {
        println("Error: ${e.message}")
    }
}

