package com.example.projectbmi.repository

import android.content.Context
import com.example.projectbmi.api.*
import com.example.projectbmi.utils.NetworkUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.util.concurrent.atomic.AtomicLong

class ChatRepository(
    private val context: Context,
    apiKey: String? = null
) {
    private val deepseekClient = DeepseekClient.getInstance(apiKey ?: DeepseekClient.API_KEY)
    private val lastRequestTime = AtomicLong(0)
    private val minRequestInterval = 1000L // Minimum 1 second between requests
    
    suspend fun validateApiKeyAndConnection(): Result<Boolean> {
        return try {
            val isValid = deepseekClient.validateApiKey()
            if (isValid) {
                Result.success(true)
            } else {
                Result.failure(Exception("API key is invalid or expired. Please check your Deepseek API key."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("Failed to connect to Deepseek service. Please check your internet connection and try again."))
        }
    }
    
    private suspend fun makeRequestWithRetry(
        request: ChatRequest,
        maxRetries: Int = 3,
        initialDelay: Long = 2000L // 2 seconds initial delay
    ): Response<ChatResponse> {
        var currentDelay = initialDelay
        repeat(maxRetries) { attempt ->
            android.util.Log.d("ChatRepository", "Attempt ${attempt + 1}/$maxRetries")
            try {
                // Rate limiting
                val timeSinceLastRequest = System.currentTimeMillis() - lastRequestTime.get()
                if (timeSinceLastRequest < minRequestInterval) {
                    android.util.Log.d("ChatRepository", "Rate limiting: waiting ${minRequestInterval - timeSinceLastRequest}ms")
                    delay(minRequestInterval - timeSinceLastRequest)
                }
                
                val response = deepseekClient.service.createChatCompletion(request)
                lastRequestTime.set(System.currentTimeMillis())
                
                when (response.code()) {
                    200 -> return response
                    429 -> {
                        if (attempt < maxRetries - 1) {
                            delay(currentDelay)
                            currentDelay *= 2 // Exponential backoff
                        }
                    }
                    else -> return response
                }
            } catch (e: Exception) {
                if (attempt == maxRetries - 1) throw e
                delay(currentDelay)
                currentDelay *= 2
            }
        }
        throw Exception("Max retries exceeded")
    }
    
    suspend fun sendMessage(userMessage: String): Result<String> = withContext(Dispatchers.IO) {
        if (!NetworkUtils.isInternetAvailable(context)) {
            return@withContext Result.failure(Exception("No internet connection available. Please check your connection and try again."))
        }

        try {
            android.util.Log.e("ChatRepository", "Starting sendMessage...")
            android.util.Log.e("ChatRepository", "Sending message: $userMessage")
            
            val messages = listOf(
                Message(
                    role = "system",
                    content = "You are a helpful fitness and health coach providing advice about BMI and healthy lifestyle. " +
                            "Your responses should be concise and practical. " +
                            "DO NOT use markdown formatting, asterisks, bullet points or special characters. " +
                            "Write in natural conversational sentences with clear paragraphs. " +
                            "Focus on giving actionable advice about BMI, nutrition, and exercise based on the user's profile context."
                ),
                Message(
                    role = "user",
                    content = userMessage
                )
            )
            
            val request = ChatRequest(messages = messages)
            android.util.Log.d("ChatRepository", "Request created: $request")
            
            val response = makeRequestWithRetry(request)
            android.util.Log.d("ChatRepository", "Response received: ${response.code()} - ${response.message()}")
        
            if (response.isSuccessful) {
                response.body()?.let { chatResponse ->
                    chatResponse.choices.firstOrNull()?.message?.content?.let { content ->
                        return@withContext Result.success(content)
                    }
                }
                return@withContext Result.failure(Exception("Invalid response format from AI"))
            } else {
                return@withContext when (response.code()) {
                    429 -> Result.failure(Exception("Too many requests. Please wait a moment and try again."))
                    500, 502, 503, 504 -> Result.failure(Exception("AI service is temporarily unavailable. Please try again later."))
                    else -> Result.failure(Exception("Error: ${response.code()} - ${response.message()}"))
                }
            }
        } catch (e: Exception) {
            return@withContext Result.failure(Exception("Failed to connect to AI service. Please check your internet connection."))
        }
    }
}