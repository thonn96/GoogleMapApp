package com.example.kotlinmap.Remote

import android.telecom.Call
import android.webkit.WebStorage
import com.example.kotlinmap.Model.MyPlaces
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface IGoogleAPIService {
    @GET
    fun getNearbyPlaces(@Url url:String):retrofit2.Call<MyPlaces>

    @GET("maps/api/directions/json")
    fun getDirections(@Query("origin") origin: String, @Query("destination") destination:String, @Query("key") apiKey: String):retrofit2.Call<String>


}