package com.neil.miruhiru.network

import com.neil.miruhiru.BuildConfig
import com.neil.miruhiru.data.SearchResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

private const val HOST_NAME = "api.mapbox.com"
private const val API = "geocoding"
private const val API_VERSION = "v5"
private const val ENDPOINT = "mapbox.places"
private const val BASE_URL = "https://$HOST_NAME/$API/$API_VERSION/$ENDPOINT/"

private val moshi = Moshi.Builder()
    .addLast(KotlinJsonAdapterFactory())
    .build()

private val client = OkHttpClient.Builder()
    .addInterceptor(
        HttpLoggingInterceptor().apply {
            level =  HttpLoggingInterceptor.Level.BODY
        }
    )
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .client(client)
    .build()

interface MapBoxApiService {

    @GET("{place}")
    suspend fun getProductList(
        @Path("place") searchedPlace: String,
        @Query("limit") limit: Int,
        @Query("access_token") accessToken: String
    ): SearchResponse
}

object MapBoxApi {
    val retrofitService: MapBoxApiService by lazy { retrofit.create(MapBoxApiService::class.java) }
}