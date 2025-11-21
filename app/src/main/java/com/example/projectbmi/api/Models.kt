package com.example.projectbmi.api

import com.google.gson.annotations.SerializedName

data class ChatRequest(
    val model: String = "deepseek-chat",
    val messages: List<Message>,
    val temperature: Double = 0.7
)

data class Message(
    val role: String,
    val content: String
)

data class ChatResponse(
    val id: String,
    @SerializedName("object") val objectType: String,
    val created: Long,
    val model: String,
    val usage: Usage,
    val choices: List<Choice>
)

data class Usage(
    @SerializedName("prompt_tokens") val promptTokens: Int,
    @SerializedName("completion_tokens") val completionTokens: Int,
    @SerializedName("total_tokens") val totalTokens: Int
)

data class Choice(
    val message: Message,
    @SerializedName("finish_reason") val finishReason: String,
    val index: Int
)