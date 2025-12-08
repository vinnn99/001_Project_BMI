package com.example.projectbmi.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context

// ╔════════════════════════════════════════════════════════════════════════════╗
// ║           MODERN AI SUGGESTIONS SECTION - Beautiful Card Design            ║
// ╚════════════════════════════════════════════════════════════════════════════╝

data class AISuggestion(
    val text: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val index: Int
)

@Composable
fun ModernAISuggestionsSection(
    tips: List<String>,
    modifier: Modifier = Modifier
) {
    if (tips.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = Color(0xFF6366F1)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "AI-Powered Tips",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937)
            )
        }

        // Suggestions cards with staggered animation
        val suggestions = tips.mapIndexed { index, tip ->
            AISuggestion(
                text = tip,
                icon = when (index % 4) {
                    0 -> Icons.Default.TrendingUp
                    1 -> Icons.Default.FitnessCenter
                    2 -> Icons.Default.Favorite
                    else -> Icons.Default.Lightbulb
                },
                gradient = when (index % 4) {
                    0 -> listOf(Color(0xFF6366F1), Color(0xFF8B5CF6)) // Purple
                    1 -> listOf(Color(0xFF3B82F6), Color(0xFF0EA5E9)) // Blue
                    2 -> listOf(Color(0xFFEC4899), Color(0xFFF43F5E)) // Pink
                    else -> listOf(Color(0xFF10B981), Color(0xFF059669)) // Green
                },
                index = index
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            suggestions.forEach { suggestion ->
                ModernSuggestionCard(suggestion)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}

private fun isNetworkAvailable(context: Context): Boolean {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = cm.activeNetwork ?: return false
    val caps = cm.getNetworkCapabilities(network) ?: return false
    val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    val validated = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    val hasTransport = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
    return (hasInternet && validated) || hasTransport
}

@Composable
private fun ModernSuggestionCard(suggestion: AISuggestion) {
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(suggestion.index) {
        delay((suggestion.index * 100).toLong())
        alpha.animateTo(1f, animationSpec = tween(400, easing = FastOutSlowInEasing))
        scale.animateTo(1f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 100f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale.value)
            .alpha(alpha.value)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = androidx.compose.material3.ripple(bounded = true),
                onClick = {}
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .background(Brush.horizontalGradient(suggestion.gradient))
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon with gradient background
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        brush = Brush.linearGradient(suggestion.gradient),
                        shape = RoundedCornerShape(10.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = suggestion.icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = Color.White
                )
            }

            // Tip text with better typography
            Text(
                text = suggestion.text,
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 14.sp,
                lineHeight = 20.sp,
                color = Color(0xFF374151),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

// ╔════════════════════════════════════════════════════════════════════════════╗
// ║            MODERN CHAT INTERFACE - Glassmorphism & Animations              ║
// ╚════════════════════════════════════════════════════════════════════════════╝

@Composable
fun ModernChatSection(
    onSendMessage: suspend (String) -> String,
    onGenerateSummary: suspend () -> Unit,
    onProgressChange: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var chatMessages by remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    var chatInput by remember { mutableStateOf("") }
    var isAITyping by remember { mutableStateOf(false) }
    var isGeneratingSummary by remember { mutableStateOf(false) }
    val chatScrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
    ) {
        // Chat Header with gradient background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF6366F1),
                            Color(0xFF8B5CF6)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    "Chat with AI Coach",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Get personalized fitness advice instantly",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }
        }

        // Chat Messages Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 350.dp)
                .background(
                    color = Color(0xFFF9FAFB),
                    shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                )
                .verticalScroll(chatScrollState)
                .padding(16.dp)
        ) {
            if (chatMessages.isEmpty() && !isAITyping) {
                // Empty State
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFF6366F1), Color(0xFF8B5CF6))
                                ),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lightbulb,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Start a conversation!",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF374151)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Ask me anything about fitness",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF6B7280)
                    )
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    chatMessages.forEach { (role, text) ->
                        when (role) {
                            "user" -> ModernUserMessage(text)
                            "ai" -> ModernAIMessage(text)
                            "error" -> ModernErrorMessage(text)
                        }
                    }

                    if (isAITyping) {
                        ModernTypingIndicator()
                    }

                    // Auto-scroll to bottom
                    LaunchedEffect(chatMessages.size) {
                        chatScrollState.animateScrollTo(chatScrollState.maxValue)
                    }
                }
            }
        }

        // Input Area
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ModernChatInputField(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    modifier = Modifier.weight(1f),
                    enabled = !isAITyping,
                    onSend = {
                        if (chatInput.isNotBlank()) {
                            // ADD USER MESSAGE IMMEDIATELY
                            val messageCopy = chatInput.trim()
                            chatMessages = chatMessages + Pair("user", messageCopy)
                            chatInput = ""
                            isAITyping = true
                            
                            scope.launch {
                                try {
                                    val response = onSendMessage(messageCopy)
                                    chatMessages = chatMessages + Pair(
                                        if (response.startsWith("❌") || response.startsWith("⏱️") || response.startsWith("⚠️")) "error" else "ai",
                                        response
                                    )
                                } catch (e: Exception) {
                                    chatMessages = chatMessages + Pair(
                                        "error",
                                        "⚠️ Error: ${e.localizedMessage ?: "Failed to get response"}"
                                    )
                                } finally {
                                    isAITyping = false
                                }
                            }
                        }
                    }
                )

                ModernSendButton(
                    isLoading = isAITyping,
                    enabled = !isAITyping && chatInput.isNotBlank(),
                    onClick = {
                        if (chatInput.isNotBlank()) {
                            // ADD USER MESSAGE IMMEDIATELY
                            val messageCopy = chatInput.trim()
                            chatMessages = chatMessages + Pair("user", messageCopy)
                            chatInput = ""
                            isAITyping = true
                            
                            scope.launch {
                                try {
                                    val response = onSendMessage(messageCopy)
                                    chatMessages = chatMessages + Pair(
                                        if (response.startsWith("❌") || response.startsWith("⏱️") || response.startsWith("⚠️")) "error" else "ai",
                                        response
                                    )
                                } catch (e: Exception) {
                                    chatMessages = chatMessages + Pair(
                                        "error",
                                        "⚠️ Error: ${e.localizedMessage ?: "Failed to get response"}"
                                    )
                                } finally {
                                    isAITyping = false
                                }
                            }
                        }
                    }
                )
            }

            // Generate Summary Button - Centered
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                contentAlignment = Alignment.Center
            ) {
                val context = LocalContext.current
                ModernGenerateSummaryButton(
                    isLoading = isGeneratingSummary,
                    enabled = chatMessages.isNotEmpty() && !isGeneratingSummary && !isAITyping && isNetworkAvailable(context),
                    onClick = {
                        val nowOnline = isNetworkAvailable(context)
                        if (chatMessages.isEmpty() || !nowOnline) {
                            if (!nowOnline) {
                                chatMessages = chatMessages + Pair(
                                    "error",
                                    "⏱️ No internet connection. Please check your network and try again."
                                )
                            }
                            return@ModernGenerateSummaryButton
                        }
                        isGeneratingSummary = true
                        scope.launch {
                            val start = System.currentTimeMillis()
                            val preCount = chatMessages.size
                            try {
                                // brief yield to render loading state immediately
                                kotlinx.coroutines.delay(100)
                                onGenerateSummary()
                                // wait until a new AI message (or error) appears in chat after generation
                                while (true) {
                                    val hasNew = chatMessages.size > preCount
                                    val last = if (hasNew) chatMessages.last() else null
                                    if (hasNew && (last?.first == "ai" || last?.first == "error")) break
                                    // also break if AI typing finished but no message added (safety)
                                    if (!isAITyping && hasNew) break
                                    kotlinx.coroutines.delay(100)
                                }
                            } finally {
                                // enforce a minimum visible loading window for UX consistency
                                val elapsed = System.currentTimeMillis() - start
                                val minVisibleMs = 800L
                                if (elapsed < minVisibleMs) {
                                    kotlinx.coroutines.delay(minVisibleMs - elapsed)
                                }
                                isGeneratingSummary = false
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ModernUserMessage(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp))
                .clip(RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF6366F1)
            )
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp, 10.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ModernAIMessage(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        // AI Avatar
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF3B82F6), Color(0xFF0EA5E9))
                    ),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(elevation = 1.dp, shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                .clip(RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Text(
                text = text,
                modifier = Modifier.padding(12.dp, 10.dp),
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF374151),
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun ModernErrorMessage(text: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFEE2E2)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(2.dp)
                .background(Color(0xFFF87171))
        )
        Text(
            text = text,
            modifier = Modifier.padding(12.dp, 10.dp),
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xDC2626),
            fontSize = 13.sp
        )
    }
}

@Composable
private fun ModernTypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF3B82F6), Color(0xFF0EA5E9))
                    ),
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Card(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .shadow(elevation = 1.dp, shape = RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp))
                .clip(RoundedCornerShape(4.dp, 16.dp, 16.dp, 16.dp)),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Row(
                modifier = Modifier.padding(14.dp, 12.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val scale = remember { Animatable(1f) }
                    LaunchedEffect(Unit) {
                        while (true) {
                            scale.animateTo(0.6f, animationSpec = tween(400))
                            scale.animateTo(1f, animationSpec = tween(400))
                            delay(800)
                        }
                    }
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF9CA3AF), RoundedCornerShape(3.dp))
                            .scale(scale.value)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernChatInputField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onSend: () -> Unit = {}
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(50.dp)
            .clip(RoundedCornerShape(12.dp)),
        placeholder = {
            Text(
                "Ask me anything...",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9CA3AF),
                fontSize = 14.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isFocused) Color(0xFF6366F1) else Color(0xFFD1D5DB)
            )
        },
        singleLine = true,
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF6366F1),
            unfocusedBorderColor = Color(0xFFE5E7EB),
            cursorColor = Color(0xFF6366F1),
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White
        ),
        interactionSource = interactionSource,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
            keyboardType = androidx.compose.ui.text.input.KeyboardType.Text,
            imeAction = androidx.compose.ui.text.input.ImeAction.Send
        ),
        keyboardActions = androidx.compose.foundation.text.KeyboardActions(
            onSend = { onSend() }
        ),
        textStyle = MaterialTheme.typography.bodySmall.copy(fontSize = 14.sp)
    )
}

@Composable
private fun ModernSendButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val scale = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()

    Button(
        onClick = {
            onClick()
            scope.launch {
                scale.animateTo(0.95f, animationSpec = tween(100))
                scale.animateTo(1f, animationSpec = tween(100))
            }
        },
        modifier = Modifier
            .size(50.dp)
            .scale(scale.value)
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF6366F1),
            disabledContainerColor = Color(0xFFD1D5DB)
        ),
        enabled = enabled,
        contentPadding = PaddingValues(0.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        } else {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                modifier = Modifier.size(22.dp),
                tint = Color.White
            )
        }
    }
}

@Composable
private fun ModernGenerateSummaryButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    
    // Use rememberInfiniteTransition for continuous animations - always active
    val infiniteTransition = rememberInfiniteTransition(label = "buttonLoading")
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    // Dot animations - always created and running
    val dot0Alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, delayMillis = 0, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot0"
    )
    
    val dot1Alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, delayMillis = 200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    
    val dot2Alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, delayMillis = 400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    
    val dotAlphas = listOf(dot0Alpha, dot1Alpha, dot2Alpha)
    
    Button(
        onClick = {
            if (!isLoading) {
                scope.launch {
                    scale.animateTo(0.95f, animationSpec = tween(100))
                    scale.animateTo(1f, animationSpec = tween(100))
                }
                onClick()
            }
        },
        modifier = Modifier
            .width(320.dp)
            .height(48.dp)
            .scale(scale.value),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color(0xFF10B981),
            disabledContainerColor = Color(0xFFD1D5DB)
        ),
        enabled = enabled
    ) {
        if (isLoading) {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .alpha(pulseAlpha)
            ) {
                // Three animated dots - centered with text
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                Color.White.copy(alpha = dotAlphas[index]),
                                RoundedCornerShape(5.dp)
                            )
                    )
                    if (index < 2) Spacer(modifier = Modifier.width(6.dp))
                }

                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Generating...",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Generate AI Summary",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = Color.White
                )
            }
        }
    }
}
