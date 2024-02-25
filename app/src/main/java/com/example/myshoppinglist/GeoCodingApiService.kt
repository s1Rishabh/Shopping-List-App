package com.example.myshoppinglist

import retrofit2.http.GET
import retrofit2.http.Query


interface GeoCodingApiService {
    @GET("maps/api/geocode/json")
    suspend fun getAddressFromCoordinates(
        @Query("latlng") latlng: String,
        @Query("key") apiKey: String
    ): GeoCodingResponse
}

