package com.example.venusai.ai

import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

// Modelos de datos para la API de OpenRouter
// Se mantiene la misma estructura que OpenAI para facilitar la integración

data class OpenRouterMessage(val role: String, val content: String)
data class OpenRouterRequest(
    val model: String,
    val messages: List<OpenRouterMessage>,
    val max_tokens: Int = 60,
    val temperature: Float = 0.7f
)
data class OpenRouterResponse(val choices: List<Choice>) {
    data class Choice(val message: OpenRouterMessage)
}

/**
 * Interfaz para la API de OpenRouter
 * Permite acceder a múltiples modelos de IA para generación de texto
 */
interface OpenRouterApi {
    @Headers("Content-Type: application/json")
    @POST("api/v1/chat/completions")
    suspend fun chat(@Body request: OpenRouterRequest): OpenRouterResponse

    companion object {
        fun create(apiKey: String): OpenRouterApi {
            val loggingInterceptor = HttpLoggingInterceptor().apply { 
                level = HttpLoggingInterceptor.Level.BODY 
            }
            
            val client = OkHttpClient.Builder()
                .addInterceptor { chain ->
                    val req = chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $apiKey")
                        .addHeader("HTTP-Referer", "https://venusai.app") // Requerido por OpenRouter
                        .build()
                    chain.proceed(req)
                }
                .addInterceptor(loggingInterceptor)
                // Configurar tiempos de espera explícitos
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                .build()
                
            return Retrofit.Builder()
                .baseUrl("https://openrouter.ai/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenRouterApi::class.java)
        }
    }
} 