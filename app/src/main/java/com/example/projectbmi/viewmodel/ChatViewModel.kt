package com.example.projectbmi.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectbmi.repository.ChatRepository
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
                                is java.net.UnknownHostException -> "Tidak dapat terhubung ke internet. Periksa koneksi Anda."
                                is retrofit2.HttpException -> {
                                    when (e.code()) {
                                        401 -> "API key tidak valid atau kadaluarsa"
                                        429 -> "Terlalu banyak permintaan. Coba lagi nanti."
                                        else -> "Terjadi kesalahan: ${e.message}"
                                    }
                                }
                                else -> e.message ?: "Terjadi kesalahan yang tidak diketahui"
                            }
                        )
                        android.util.Log.e("ChatViewModel", "Error validating API key", e)
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Terjadi kesalahan yang tidak terduga: ${e.message}"
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
                    val assistantMessage = ChatMessage(response, isUserMessage = false)
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
}