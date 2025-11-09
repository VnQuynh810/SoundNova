package com.example.soundnova.service

import com.example.soundnova.models.Tracks
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


interface DeezerSearchAPI {
        @GET("search")
        suspend fun searchTracks(@Query("q") query: String): Response<Tracks>
    }

    object DeezerSearchApiHelper {
        private val retrofit = Retrofit.Builder()
            .baseUrl("https://api.deezer.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        private val api = retrofit.create(DeezerSearchAPI::class.java)

        @Throws(RuntimeException::class)
        suspend fun fetchTracks(input : String): Tracks {
            val response = api.searchTracks(input)
            if (response.isSuccessful) {
                return response.body() ?: throw RuntimeException("No data")
            } else {
                throw RuntimeException("API call failed: ${response.errorBody()?.string()}")
            }
        }
    }

    suspend fun main() {
        var deezerSearchAPI = DeezerSearchApiHelper
        println(deezerSearchAPI.fetchTracks(""))
    }
