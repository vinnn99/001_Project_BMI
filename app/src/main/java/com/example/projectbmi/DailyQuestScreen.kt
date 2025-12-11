package com.example.projectbmi

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.example.projectbmi.model.QuestTask
import com.example.projectbmi.viewmodel.HistoryViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyQuestScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("daily_quest_prefs", Context.MODE_PRIVATE)
    val userId = FirebaseAuth.getInstance().currentUser?.uid
    val historyViewModel = remember { if (userId != null) HistoryViewModel(userId) else null }

    val loading = remember { mutableStateOf(false) }
    val schedule = remember { mutableStateOf<List<QuestTask>>(emptyList()) }
    val completed = remember { mutableStateOf(setOf<Int>()) }
    val scope = rememberCoroutineScope()

    // Load saved schedule and completed flags on first composition
    LaunchedEffect(Unit) {
        loading.value = true
        try {
            val hasOldFormat = prefs.contains("weekly_schedule")
            if (hasOldFormat) {
                prefs.edit().remove("weekly_schedule").apply()
            }
            
            val json = prefs.getString("weekly_schedule_json", "") ?: ""
            schedule.value = if (json.isBlank()) {
                emptyList()
            } else {
                try {
                    Gson().fromJson(json, Array<QuestTask>::class.java).toList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
            
            val todayDate = LocalDate.now().toString()
            val savedDate = prefs.getString("daily_completed_date", "") ?: ""
            
            if (todayDate != savedDate) {
                prefs.edit()
                    .remove("daily_completed")
                    .putString("daily_completed_date", todayDate)
                    .apply()
                completed.value = emptySet()
            } else {
                val compStr = prefs.getString("daily_completed", "") ?: ""
                if (compStr.isNotBlank()) {
                    val loaded = compStr.split(",").mapNotNull { it.toIntOrNull() }.toSet()
                    completed.value = loaded
                }
            }
        } catch (_: Exception) {
        }
        loading.value = false
    }

    fun persistSchedule(list: List<QuestTask>) {
        try {
            val json = Gson().toJson(list)
            prefs.edit().putString("weekly_schedule_json", json).apply()
        } catch (_: Exception) {}
    }

    fun persistCompleted() {
        try {
            val completedStr = completed.value.joinToString(",")
            val todayDate = LocalDate.now().toString()
            prefs.edit()
                .putString("daily_completed", completedStr)
                .putString("daily_completed_date", todayDate)
                .apply()
        } catch (_: Exception) {}
    }
    
    fun toggleTask(index: Int, taskName: String) {
        val newSet = completed.value.toMutableSet()
        val isCompleting = !completed.value.contains(index)
        
        if (newSet.contains(index)) {
            newSet.remove(index)
        } else {
            newSet.add(index)
        }
        
        completed.value = newSet.toSet()
        persistCompleted()
        
        if (isCompleting && historyViewModel != null) {
            scope.launch {
                historyViewModel.logQuestCompletion(taskName, index.toString())
            }
        }
    }

    // Color palette
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF6366F1),  // Indigo
            Color(0xFFA855F7)   // Purple
        )
    )

    val accentGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF3B82F6),  // Blue
            Color(0xFF8B5CF6)   // Purple
        )
    )

    val progressGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF10B981),  // Green
            Color(0xFF14B8A6)   // Teal
        )
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(gradientBrush),
        color = Color.Transparent
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Daily Quest",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.navigateUp() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            },
            containerColor = Color.Transparent,
            contentColor = Color.White
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Weekly Tasks Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_view),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        "Weekly Tasks",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                // Progress Bar
                if (schedule.value.isNotEmpty()) {
                    val totalTasks = schedule.value.size
                    val completedCount = completed.value.size.coerceAtMost(totalTasks)
                    val progress = completedCount.toFloat() / totalTasks.toFloat()

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .padding(bottom = 8.dp),
                        color = Color(0xFF10B981),
                        trackColor = Color.White.copy(alpha = 0.3f)
                    )
                    Text(
                        "Progress: ${(progress * 100).toInt()}%",
                        fontSize = 13.sp,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (loading.value) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .padding(32.dp)
                            .size(48.dp),
                        color = Color.White
                    )
                } else {
                    // EMPTY STATE
                    if (schedule.value.isEmpty()) {
                        EmptyStateCard(navController)
                    } else {
                        // SCHEDULE STATE
                        ScheduleCard(
                            schedule = schedule.value,
                            completed = completed.value,
                            onToggleTask = { index, taskName ->
                                toggleTask(index, taskName)
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Today's Goal Card
                        TodayGoalCard(schedule = schedule.value)

                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }

                // Clear Button
                if (schedule.value.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                schedule.value = emptyList()
                                completed.value = emptySet()
                                try { 
                                    prefs.edit()
                                        .remove("weekly_schedule_json")
                                        .remove("daily_completed")
                                        .remove("daily_completed_date")
                                        .apply() 
                                } catch (_: Exception) {}
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(end = 4.dp, bottom = 16.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Clear", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard(navController: NavController) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(initialOffsetY = { 30 })
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Icon
                Surface(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF3B82F6),
                                    Color(0xFF8B5CF6)
                                )
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ),
                    color = Color.Transparent,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_agenda),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }

                // Heading
                Text(
                    "No Weekly Schedule Yet",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                // Description
                Text(
                    "Start your personalized fitness journey by visiting Ask AI to generate your weekly schedule.",
                    fontSize = 14.sp,
                    color = Color(0xFF6B7280),
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                // CTA Button
                Button(
                    onClick = { navController.navigate("askAI") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF3B82F6)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = android.R.drawable.ic_menu_view),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        "Go to Ask AI",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ScheduleCard(
    schedule: List<QuestTask>,
    completed: Set<Int>,
    onToggleTask: (Int, String) -> Unit
) {
    val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
    val todayIdx = (LocalDate.now().dayOfWeek.value - 1) % 7

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            schedule.forEachIndexed { idx, task ->
                val isDone = completed.contains(idx)
                val isToday = idx == todayIdx
                val dayLabel = days.getOrNull(idx) ?: "-"

                task?.let { t ->
                    TaskRow(
                        dayLabel = dayLabel,
                        taskTitle = t.task,
                        duration = t.duration,
                        isChecked = isDone,
                        isToday = isToday,
                        isCompleted = isDone,
                        onCheck = { onToggleTask(idx, t.task) }
                    )

                    if (idx < schedule.size - 1) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Spacer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFE5E7EB))
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    dayLabel: String,
    taskTitle: String,
    duration: Int,
    isChecked: Boolean,
    isToday: Boolean,
    isCompleted: Boolean,
    onCheck: (Boolean) -> Unit
) {
    val dayBgColor = when {
        isToday && !isCompleted -> Color(0xFF6366F1)
        isCompleted -> Color(0xFF10B981)
        else -> Color(0xFFF3F4F6)
    }

    val dayTextColor = when {
        isToday && !isCompleted -> Color.White
        isCompleted -> Color.White
        else -> Color(0xFF1F2937)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Checkbox
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheck,
            enabled = isToday,
            modifier = Modifier.size(24.dp),
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF10B981),
                uncheckedColor = Color(0xFF6366F1)
            )
        )

        // Day Badge
        Surface(
            modifier = Modifier
                .size(width = 50.dp, height = 32.dp)
                .background(dayBgColor, shape = RoundedCornerShape(8.dp)),
            color = dayBgColor,
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .size(width = 50.dp, height = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    dayLabel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = dayTextColor,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Task Details
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                taskTitle,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF1F2937),
                maxLines = 2
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    painter = painterResource(id = android.R.drawable.ic_menu_info_details),
                    contentDescription = null,
                    tint = Color(0xFF6B7280),
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "Duration: $duration min",
                    fontSize = 13.sp,
                    color = Color(0xFF6B7280)
                )
            }
        }
    }
}

@Composable
private fun TodayGoalCard(schedule: List<QuestTask>) {
    val todayIdx = (LocalDate.now().dayOfWeek.value - 1) % 7
    val todayTask = schedule.getOrNull(todayIdx)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF6366F1),
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    "Today's Goal",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1F2937)
                )
            }

            // Content
            if (todayTask != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF6366F1).copy(alpha = 0.08f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        todayTask.task,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF1F2937)
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_info_details),
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            "${todayTask.duration} minutes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF6366F1)
                        )
                    }
                }
            } else {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFFECACA),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(12.dp),
                    color = Color.Transparent
                ) {
                    Text(
                        "No task for today. Enjoy your rest day! 🎉",
                        fontSize = 15.sp,
                        color = Color(0xFF7F1D1D),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
