package com.example.kotlinmap.Common

import com.example.kotlinmap.Model.Results
import com.example.kotlinmap.Remote.IGoogleAPIService
import com.example.kotlinmap.Remote.RetrofitClient
import com.example.kotlinmap.Remote.RetrofitScalarsClient

object Common {
    private val GOOGLE_API_URL="https://maps.googleapis.com/"
    val googleApiService:IGoogleAPIService
    get()=RetrofitClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService::class.java)
    var currentResult:Results?=null

    val googleApiServiceScalars:IGoogleAPIService
        get()=RetrofitScalarsClient.getClient(GOOGLE_API_URL).create(IGoogleAPIService::class.java)


}