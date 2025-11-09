package com.example.soundnova.models

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface DeezerCategoryAPI {
    @GET("genre")
    suspend fun getGenres(): Response<GenresResponse>
}

object DeezerCategoryApiHelper {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.deezer.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(DeezerCategoryAPI::class.java)

    @Throws(RuntimeException::class)
    suspend fun fetchGenres(): GenresResponse {
        val response = api.getGenres()
        if (response.isSuccessful) {
            return response.body() ?: throw RuntimeException("No data")
        } else {
            throw RuntimeException("API call failed: ${response.errorBody()?.string()}")
        }
    }
}

suspend fun main() {
    var deezerCategoryApiHelper = DeezerCategoryApiHelper
    println(deezerCategoryApiHelper.fetchGenres())
}