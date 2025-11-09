package com.example.soundnova.service

import com.example.soundnova.models.Albums
import com.example.soundnova.models.AlbumsResponse
import com.example.soundnova.models.Artist
import com.example.soundnova.models.Artists
import com.example.soundnova.models.TrackData
import com.example.soundnova.models.Tracks
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path

interface DeezerApi {

    @GET("chart/0/tracks")
    suspend fun getPopularTracks() : Response<Tracks>
//
//    @GET("chart/0/albums")
//    suspend fun getPopularAlbums() : Response<Albums>

    @GET("artist/10803980/albums")
    suspend fun getPopularAlbums() : Response<Albums>

    @GET("artist/10803980")
    suspend fun getArtist() : Response<Artist>


    @GET("chart/0/artists")
    suspend fun getPopularArtists() : Response<Artists>


    @GET("artist/{artist_id}/top?limit=50")
    suspend fun getTracksOfArtist(@Path("artist_id") artistID: Long) : Response<Tracks>

    @GET("album/{album_id}/tracks")
    suspend fun getTracksOfAlbum(@Path("album_id") albumID: Long) : Response<Tracks>

    @GET("track/{track_id}")
    suspend fun getTrack(@Path("track_id") trackId: String): Response<TrackData>
}
object DeezerApiHelper {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.deezer.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(DeezerApi::class.java)

    @Throws(RuntimeException::class)
    suspend fun fetchPopularTracks(): Tracks {
        val response = api.getPopularTracks()
        if (response.isSuccessful) {
            return response.body() ?: throw RuntimeException("No data")
        } else {
            throw RuntimeException("API call failed: ${response.errorBody()?.string()}")
        }
    }

    @Throws(RuntimeException::class)
    suspend fun getTrack(trackId: String): TrackData {
        val response = api.getTrack(trackId)
        if (response.isSuccessful) {
            return response.body() ?: throw RuntimeException("No data")
        } else {
            throw RuntimeException("API call failed: ${response.errorBody()?.string()}")
        }
    }

    @Throws(RuntimeException::class)
    suspend fun fetchPopularAlbums(): Albums {
        val response = api.getPopularAlbums()
        if (response.isSuccessful) {
            return response.body() ?: throw RuntimeException("No data")
        } else {
            throw RuntimeException("API call failed: ${response.errorBody()?.string()}")
        }
    }

    @Throws(RuntimeException::class)
    suspend fun fetchPopularArtists(): Artists {
        val response = api.getPopularArtists()
        if (response.isSuccessful) {
            return response.body() ?: throw RuntimeException("No data")
        } else {
            throw RuntimeException("API call failed: ${response.errorBody()?.string()}")
        }
    }

    @Throws(RuntimeException::class)
    suspend fun getTracksOfArtist(artistID: Long): Tracks {
        val response = api.getTracksOfArtist(artistID)
        if (response.isSuccessful) {
            return response.body() ?: throw RuntimeException("No data")
        } else {
            throw RuntimeException("API call failed: ${response.errorBody()?.string()}")
        }
    }

    @Throws(RuntimeException::class)
    suspend fun getTracksOfAlbum(albumID: Long): Tracks {
        val response = api.getTracksOfAlbum(albumID)
        if (response.isSuccessful) {
            return response.body() ?: throw RuntimeException("No data")
        } else {
            throw RuntimeException("API call failed: ${response.errorBody()?.string()}")
        }
    }

    @Throws(RuntimeException::class)
    suspend fun getArtist(): Artist {
        val response = api.getArtist()
        if (response.isSuccessful) {
            return response.body() ?: throw RuntimeException("No data")
        } else {
            throw RuntimeException("API call failed: ${response.errorBody()?.string()}")
        }
    }
}

suspend fun main() {
    val deezerApiHelper = DeezerApiHelper
//    println(deezerApiHelper.fetchPopularAlbums())
//    println(deezerApiHelper.fetchPopularArtists())
    println(deezerApiHelper.getTrack("2830386932"))

}