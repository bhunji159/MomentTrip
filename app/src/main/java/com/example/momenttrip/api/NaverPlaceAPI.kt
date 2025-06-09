package com.example.momenttrip.api

import retrofit2.http.GET
import retrofit2.http.Query

interface NaverPlaceAPI {
    interface NaverPlaceApi {
        @GET("map-place/v1/search")
        suspend fun searchPlace(
            @Query("query") query: String,
            @Query("coordinate") coordinate: String = "127.1054328,37.3595963", // 기본 위치
            @Query("radius") radius: Int = 5000
        ): PlaceSearchResponse
    }

}