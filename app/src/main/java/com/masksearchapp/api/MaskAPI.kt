package com.masksearchapp.api

import com.masksearchapp.data.MaskData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

//https://app.swaggerhub.com/apis-docs/Promptech/public-mask-info/20200307-oas3#/v1/get_storesByGeo_json

interface MaskAPI {

    companion object {
        const val base_Url = "https://8oi9s0nnth.apigw.ntruss.com/"
//        const val CLIENT_ID =

    }

    @Headers("Accept: application/json")
    @GET("corona19-masks/v1/storesByGeo/json")
    fun searchMask(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double,
        @Query("m") m: Int

    ): Call<MaskData>

}