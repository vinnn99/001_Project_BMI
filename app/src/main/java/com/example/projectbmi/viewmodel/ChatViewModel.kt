package com.example.projectbmi.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectbmi.repository.ChatRepository
import com.example.projectbmi.utils.TextUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ChatMessage(
    val content: String,
    val isUserMessage: Boolean
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = ChatRepository(application.applicationContext)
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    val userInput = mutableStateOf("")
    
    init {
        validateApiKey()
    }
    
    private fun validateApiKey() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                repository.validateApiKeyAndConnection()
                    .onSuccess {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                    .onFailure { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = when (e) {
                                is java.net.UnknownHostException -> "Cannot connect to the internet. Please check your connection."
                                is retrofit2.HttpException -> {
                                    when (e.code()) {
                                        401 -> "API key is invalid or expired"
                                        429 -> "Too many requests. Please try again later."
                                        else -> "Error: ${e.message}"
                                    }
                                }
                                else -> e.message ?: "An unknown error occurred"
                            }
                        )
                        android.util.Log.e("ChatViewModel", "Error validating API key", e)
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unexpected error: ${e.message}"
                )
                android.util.Log.e("ChatViewModel", "Unexpected error", e)
            }
        }
    }
    
    fun sendMessage(message: String) {
        if (message.isBlank()) return
        
        val userMessage = ChatMessage(message, isUserMessage = true)
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            isLoading = true,
            error = null
        )
        
        viewModelScope.launch {
            repository.sendMessage(message)
                .onSuccess { response ->
                    val cleaned = TextUtils.sanitizeAssistantText(response)
                    val assistantMessage = ChatMessage(cleaned, isUserMessage = false)
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + assistantMessage,
                        isLoading = false
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = throwable.message ?: "An unknown error occurred"
                    )
                }
        }
        
        userInput.value = ""
    }

    // Send a system prompt that should NOT be shown as a user message in the UI
    fun sendSystemPrompt(systemPrompt: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.sendSystemPrompt(systemPrompt)
                .onSuccess { response ->
                    val cleaned = TextUtils.sanitizeAssistantText(response)
                    val assistantMessage = ChatMessage(cleaned, isUserMessage = false)
                    _uiState.value = _uiState.value.copy(
                        messages = _uiState.value.messages + assistantMessage,
                        isLoading = false
                    )
                }
                .onFailure { throwable ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = throwable.message ?: "An unknown error occurred"
                    )
                }
        }
    }
}