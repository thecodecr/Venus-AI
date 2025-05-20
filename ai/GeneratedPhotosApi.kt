package com.example.venusai.ai

import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

data class FaceAttributes(
    val gender: String,
    val age: Int,
    val ethnicity: String
)

data class FaceImage(
    val id: String,
    val url: String,
    val attributes: FaceAttributes
)

data class GeneratedPhotoResponse(
    val total: Int,
    val faces: List<FaceImage>
)

interface GeneratedPhotosApi {
    @Headers("Content-Type: application/json")
    @GET("v3/faces")
    suspend fun getFaces(
        @Query("gender") gender: String? = null,  // male, female
        @Query("age") age: String? = null,        // infant, child, young-adult, adult, elderly
        @Query("ethnicity") ethnicity: String? = null,
        @Query("emotion") emotion: String? = null,
        @Query("per_page") perPage: Int = 1
    ): GeneratedPhotoResponse

    companion object {
        fun create(apiKey: String): GeneratedPhotosApi {
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val req = chain.request().newBuilder()
                        .addHeader("Authorization", "API-Key $apiKey")
                        .build()
                    chain.proceed(req)
                }
                .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                .build()
            return Retrofit.Builder()
                .baseUrl("https://api.generated.photos/api/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(GeneratedPhotosApi::class.java)
        }
    }
} 