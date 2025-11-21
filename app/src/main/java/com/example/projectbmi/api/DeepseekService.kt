package com.example.projectbmi.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface DeepseekService {
    @POST("chat/completions")
    suspend fun createChatCompletion(
        @Body request: ChatRequest
    ): Response<ChatResponse>
}