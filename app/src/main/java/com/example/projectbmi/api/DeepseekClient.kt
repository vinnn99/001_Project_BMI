package com.example.projectbmi.api

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

class DeepseekClient(private val apiKey: String) {

    private val baseUrl = "https://api.deepseek.com/v1/"

    suspend fun validateApiKey(): Boolean {
        try {
            android.util.Log.d("Deepseek", "Validating API key...")
            val testRequest = ChatRequest(
                messages = listOf(
                    Message(
                        role = "user",
                        content = "Hi"
                    )
                ),
                model = "deepseek-chat",
                temperature = 0.7
            )

            val response = service.createChatCompletion(testRequest)

            return when (response.code()) {
                200 -> true
                401 -> false
                429 -> true
                else -> false
            }
        } catch (e: Exception) {
            android.util.Log.e("Deepseek", "Error validating key", e)
            return false
        }
    }

    private val loggingInterceptor = okhttp3.logging.HttpLoggingInterceptor().apply {
        level = okhttp3.logging.HttpLoggingInterceptor.Level.BODY
    }

    private val debugInterceptor = Interceptor { chain ->
        val request = chain.request()
        android.util.Log.d("Deepseek", "Request: ${request.url}")
        chain.proceed(request)
    }

    private val authInterceptor = Interceptor { chain ->
        val original = chain.request()
        val request = original.newBuilder()
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .build()
        chain.proceed(request)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(debugInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(120, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(baseUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service: DeepseekService = retrofit.create(DeepseekService::class.java)

    companion object {
        @Volatile
        private var instance: DeepseekClient? = null

        fun getInstance(): DeepseekClient {
            val key = BuildConfig.DEEPSEEK_API_KEY
            return instance ?: synchronized(this) {
                instance ?: DeepseekClient(key).also { instance = it }
            }
        }
    }
}
