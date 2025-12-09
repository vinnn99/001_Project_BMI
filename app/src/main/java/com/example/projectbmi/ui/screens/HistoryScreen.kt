package com.example.projectbmi.ui.screens

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.projectbmi.model.HistoryUiState
import com.example.projectbmi.model.QuestHistoryDay
import com.example.projectbmi.model.StreakData
import com.example.projectbmi.viewmodel.HistoryViewModel
import com.google.firebase.auth.FirebaseAuth
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun HistoryScreen(
    onBackClick: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val viewModel = remember { HistoryViewModel(userId) }
    val currentUiState by viewModel.uiState.collectAsState()
    val isLoading = currentUiState is HistoryUiState.Loading
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header with logout button
            HeaderSection(onBackClick = onBackClick, onLogout = onLogout)
            
            // Content
            when (currentUiState) {
                is HistoryUiState.Loading -> {
                    LoadingState()
                }
                is HistoryUiState.Success -> {
                    val success = currentUiState as HistoryUiState.Success
                    HistoryContent(
                        streakData = success.streakData,
                        historyDays = success.historyDays
                    )
                }
                is HistoryUiState.Empty -> {
                    EmptyState()
                }
                is HistoryUiState.Error -> {
                    val error = currentUiState as HistoryUiState.Error
                    ErrorState(message = error.message)
                }
            }
        }
    }
}

@Composable
private fun HeaderSection(onBackClick: () -> Unit, onLogout: () -> Unit) {
    val context = LocalContext.current
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            IconButton(onClick = onBackClick, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color(0xFF212121),
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                "History",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF212121),
                fontSize = 24.sp
            )
        }
        
        IconButton(
            onClick = {
                // Clear SharedPreferences auth flag
                val prefs = context.getSharedPreferences("app_auth_state", android.content.Context.MODE_PRIVATE)
                prefs.edit().putBoolean("wasLoggedIn", false).apply()
                // Sign out from Firebase
                com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                onLogout()
            },
            modifier = Modifier.size(40.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ExitToApp,
                contentDescription = "Logout",
                tint = Color(0xFFE53935),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun HistoryContent(
    streakData: StreakData,
    historyDays: List<QuestHistoryDay>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        // Stats Strip
        item {
            StatsStrip(streakData)
        }

        // Divider
        item {
            Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
        }

        // Streak Card
        item {
            StreakCard(streakData)
        }

        // Quest History Title
        item {
            Text(
                "Quest History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF212121),
                fontSize = 16.sp
            )
        }

        // Group history by month
        val groupedByMonth = historyDays.groupBy { day ->
            YearMonth.parse(day.date.substring(0, 7))
        }

        groupedByMonth.forEach { (yearMonth, daysInMonth) ->
            item {
                Text(
                    yearMonth.format(DateTimeFormatter.ofPattern("MMM yyyy")).uppercase(),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF999999),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(daysInMonth) { day ->
                QuestHistoryItem(day)
            }
        }
    }
}

@Composable
private fun StatsStrip(streakData: StreakData) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(
            icon = "🔥",
            label = "Streak",
            value = "${streakData.currentStreak} days"
        )
        StatItem(
            icon = "📅",
            label = "Total",
            value = "${streakData.totalCompleted} quests"
        )
        StatItem(
            icon = "🎯",
            label = "This Month",
            value = "${streakData.thisMonthCompleted} quests"
        )
    }
}

@Composable
private fun StatItem(icon: String, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            icon,
            fontSize = 24.sp
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF999999),
            fontSize = 11.sp
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF212121),
            fontSize = 12.sp
        )
    }
}

@Composable
private fun StreakCard(streakData: StreakData) {
    var isLoaded by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isLoaded = true
    }
    
    val scaleAnimation by animateFloatAsState(
        targetValue = if (isLoaded) 1.2f else 1.0f,
        animationSpec = tween(durationMillis = 500),
        label = "streakScale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFF6B6B), Color(0xFFFFA500))
                    )
                )
                .padding(vertical = 24.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    "🔥",
                    fontSize = 48.sp,
                    modifier = Modifier.scale(scaleAnimation)
                )
                Text(
                    "STREAK",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 16.sp
                )
                Text(
                    "${streakData.currentStreak}",
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 48.sp
                )
                Text(
                    "days in a row",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    fontSize = 14.sp
                )
                Text(
                    if (streakData.currentStreak > 0) "Keep it going!" else "Start today!",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
private fun QuestHistoryItem(day: QuestHistoryDay) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Date header
        Text(
            "${ java.time.LocalDate.parse(day.date).format(java.time.format.DateTimeFormatter.ofPattern("MMM d, EEEE"))}",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF666666),
            fontSize = 14.sp
        )

        // Quests for this day
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            day.quests.forEach { quest ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Checkmark
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                color = Color(0xFF4CAF50),
                                shape = RoundedCornerShape(50)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Quest name and time
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Text(
                            quest.questName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF212121),
                            fontSize = 16.sp
                        )
                        quest.completedAt?.let {
                            val time = it.toDate()
                            val formatter = java.time.format.DateTimeFormatter.ofPattern("HH:mm")
                            val localTime = java.time.LocalDateTime
                                .ofInstant(time.toInstant(), java.time.ZoneId.systemDefault())
                                .format(formatter)
                            
                            Text(
                                "Completed at $localTime",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF999999),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Item divider
        Divider(color = Color(0xFFE0E0E0), thickness = 1.dp)
    }
}

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.FavoriteBorder,
            contentDescription = "No history",
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFCCCCCC)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "No history records yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF212121),
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Complete your first daily quest to start tracking!",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF666666),
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun LoadingState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = Color(0xFF667EEA),
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Loading history...",
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF999999)
        )
    }
}

@Composable
private fun ErrorState(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = "Error",
            modifier = Modifier.size(64.dp),
            tint = Color(0xFFFF6B6B)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Oops! Something went wrong",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF212121),
            fontSize = 18.sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF666666),
            fontSize = 14.sp,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
