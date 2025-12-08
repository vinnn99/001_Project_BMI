package com.example.projectbmi

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

object DeepseekRepository {
    private const val DEEPSEEK_API_URL = "https://api.deepseek.com/chat/completions"
    // API key diambil dari BuildConfig yang di-generate dari local.properties
    private val API_KEY: String
        get() = BuildConfig.DEEPSEEK_API_KEY

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Send a message to Deepseek API and get a streaming response.
     * Returns the complete AI response text or a user-friendly error message.
     */
    suspend fun sendMessage(userMessage: String, context: String = ""): String = withContext(Dispatchers.IO) {
        try {
            // Validate API key first
            if (API_KEY.isBlank() || API_KEY == "") {
                android.util.Log.e("DeepseekRepository", "API_KEY is empty or not set!")
                return@withContext "❌ API key not configured. Please add DEEPSEEK_API_KEY to local.properties"
            }
            
            android.util.Log.d("DeepseekRepository", "Sending message: $userMessage")
            
            val systemPrompt = """
                You are a health and fitness coach assistant helping users with BMI tracking and wellness.
                Keep responses concise (1-2 sentences max) and actionable.
                Focus on encouraging small, sustainable habits.
                $context
            """.trimIndent()

            val payload = JSONObject().apply {
                put("model", "deepseek-chat")
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemPrompt)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", userMessage)
                    })
                })
                put("temperature", 0.7)
                put("max_tokens", 150)
                put("stream", false)
            }

            val requestBody = payload.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(DEEPSEEK_API_URL)
                .header("Authorization", "Bearer $API_KEY")
                .header("Content-Type", "application/json")
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: return@withContext "No response"
                val jsonResponse = JSONObject(responseBody)
                val choices = jsonResponse.optJSONArray("choices")
                if (choices != null && choices.length() > 0) {
                    val choice = choices.getJSONObject(0)
                    val message = choice.getJSONObject("message")
                    return@withContext message.optString("content", "I couldn't process that. Please try again.")
                } else {
                    return@withContext "No choices in response"
                }
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                return@withContext "API Error: ${response.code} - $errorBody"
            }
        } catch (e: UnknownHostException) {
            android.util.Log.e("DeepseekRepository", "UnknownHostException: ${e.message}", e)
            return@withContext "❌ No internet connection. Please check your network."
        } catch (e: SocketTimeoutException) {
            android.util.Log.e("DeepseekRepository", "SocketTimeoutException: ${e.message}", e)
            return@withContext "⏱️ Weak connection or timeout. Please try again later."
        } catch (e: java.net.ConnectException) {
            android.util.Log.e("DeepseekRepository", "ConnectException: ${e.message}", e)
            return@withContext "❌ Unable to connect. Check your internet connection."
        } catch (e: javax.net.ssl.SSLException) {
            android.util.Log.e("DeepseekRepository", "SSLException: ${e.message}", e)
            return@withContext "🔐 Connection security issue. Please check your internet connection."
        } catch (e: java.io.IOException) {
            android.util.Log.e("DeepseekRepository", "IOException: ${e.message}", e)
            return@withContext "❌ Network error: ${e.message ?: "Connection failed"}"
        } catch (e: Exception) {
            android.util.Log.e("DeepseekRepository", "Unexpected error: ${e.message}", e)
            e.printStackTrace()
            return@withContext "⚠️ Error: ${e.message ?: "An error occurred"}"
        }
    }
}
