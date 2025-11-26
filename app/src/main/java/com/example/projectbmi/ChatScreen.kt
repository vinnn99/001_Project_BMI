package com.example.projectbmi

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectbmi.viewmodel.ChatViewModel
import com.example.projectbmi.viewmodel.ChatMessage

class ChatViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    initialContext: String
) {
    val context = LocalContext.current
    val viewModelFactory = remember { ChatViewModelFactory(context.applicationContext as Application) }
    val chatViewModel: ChatViewModel = viewModel(factory = viewModelFactory)
    val uiState by chatViewModel.uiState.collectAsState()
    val messageText by chatViewModel.userInput

    // Send a concise context prompt once so the AI reply appears as the first message
    LaunchedEffect(Unit) {
        if (uiState.messages.isEmpty()) {
            // Provide a clearer system prompt: include context and explicit formatting instructions
            val prompt = """
                Based on your profile:
                $initialContext

                Please provide personalized coaching advice for improving health and fitness. Consider all aspects of the profile including BMI, goals, current exercise habits, diet preferences, sleep patterns, and any challenges faced.
            """.trimIndent()
            // Send as system prompt so it will not be shown as a user message; assistant reply will appear
            chatViewModel.sendSystemPrompt(prompt)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat with AI Coach") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Messages area (oldest at top, newest at bottom). Use a LazyListState so user can scroll up.
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.messages) { message ->
                    ChatBubble(message)
                }
            }

            // Auto-scroll to bottom when a new message arrives
            LaunchedEffect(uiState.messages.size) {
                if (uiState.messages.isNotEmpty()) {
                    try {
                        listState.animateScrollToItem(uiState.messages.lastIndex)
                    } catch (e: Exception) {
                        // ignore exceptions from scrolling during composition changes
                    }
                }
            }

            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            // Error message if any — show friendly guidance and disable input for auth/connectivity issues
            val errorMsg = uiState.error
            val isAuthError = errorMsg?.contains("API key", ignoreCase = true) == true || errorMsg?.contains("401") == true
            val isNetworkError = errorMsg?.contains("internet", ignoreCase = true) == true || errorMsg?.contains("UnknownHostException", ignoreCase = true) == true
            errorMsg?.let { error ->
                val friendly = when {
                    isAuthError -> "Chat disabled: API key is not configured or invalid. Please add `DEEPSEEK_API_KEY` to your local `gradle.properties` or set it as an environment variable."
                    isNetworkError -> "Cannot connect: please check your internet connection."
                    else -> error
                }

                Text(
                    text = friendly,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // Input area
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { chatViewModel.userInput.value = it },
                    placeholder = { Text("Type your question...") },
                    modifier = Modifier.weight(1f),
                    maxLines = 3,
                    enabled = uiState.error == null || !(uiState.error?.contains("401") == true || uiState.error?.contains("API key", ignoreCase = true) == true)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = {
                        if (messageText.isNotBlank()) {
                            chatViewModel.sendMessage(messageText)
                        }
                    },
                    enabled = (uiState.error == null || !isAuthError) && !uiState.isLoading && messageText.isNotBlank(),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Default.Send,
                        contentDescription = "Send",
                        tint = MaterialTheme.colorScheme.onTertiary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val backgroundColor = if (message.isUserMessage) {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val alignment = if (message.isUserMessage) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Card(
            modifier = Modifier.padding(vertical = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(message.content)
            }
        }
    }
}