package com.example.projectbmi

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
// removed weight import to avoid internal API issues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Checkbox
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import com.google.gson.Gson
import com.example.projectbmi.model.QuestTask
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
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
            // Clear old format data to avoid confusion
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
            
            // Load completed tasks with date check - reset if different day
            val todayDate = LocalDate.now().toString()
            val savedDate = prefs.getString("daily_completed_date", "") ?: ""
            
            if (todayDate != savedDate) {
                // Different day - clear old completed tasks
                prefs.edit()
                    .remove("daily_completed")
                    .putString("daily_completed_date", todayDate)
                    .apply()
                completed.value = emptySet()
            } else {
                // Same day - load saved completed tasks
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
        
        // Log to Firestore if completing and user is authenticated
        if (isCompleting && historyViewModel != null) {
            scope.launch {
                historyViewModel.logQuestCompletion(taskName, index.toString())
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Quest") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            painter = painterResource(id = android.R.drawable.ic_media_previous),
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(padding)
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Weekly Tasks", color = Color(0xFF333333))
            }

            // Progress bar showing completion ratio
            val totalTasks = schedule.value.size.coerceAtLeast(1)
            val completedCount = completed.value.size.coerceAtMost(totalTasks)
            val progress = if (schedule.value.isEmpty()) 0f else (completedCount.toFloat() / totalTasks.toFloat())

            LinearProgressIndicator(progress = progress, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
            Text("Progress: ${ (progress * 100).toInt() }%", color = Color(0xFF333333))

            if (loading.value) {
                CircularProgressIndicator(modifier = Modifier.padding(12.dp))
            }

            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), color = Color(0xFFF8FAF0)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    val days = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
                    val todayIdx = (LocalDate.now().dayOfWeek.value - 1) % 7  // Monday = 0, Sunday = 6
                    if (schedule.value.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                "📋 No Weekly Schedule Yet",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333)
                            )
                            Text(
                                "Start your personalized fitness journey by visiting Ask AI to generate your weekly schedule.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF666666),
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = { navController.navigate("askAI") },
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text("Go to Ask AI →")
                            }
                        }
                    } else {
                        schedule.value.forEachIndexed { idx, task ->
                            val isDone = completed.value.contains(idx)
                            val isToday = idx == todayIdx
                            task?.let { t ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = days.getOrNull(idx) ?: "-",
                                        modifier = Modifier.size(44.dp),
                                        color = Color(0xFF444444)
                                    )
                                    Checkbox(
                                        checked = isDone,
                                        onCheckedChange = { _ ->
                                            toggleTask(idx, t.task)
                                        },
                                        enabled = isToday
                                    )
                                    Spacer(modifier = Modifier.size(8.dp))
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 12.dp)
                                    ) {
                                        Text("Task: ${t.task}")
                                        Text("Duration: ${t.duration} min", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                Button(onClick = {
                    // Clear saved schedule and completions
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
                }) {
                    Text("Clear")
                }
            }

            Spacer(modifier = Modifier.padding(12.dp))

            // Quick today's task preview - only show if schedule exists
            if (schedule.value.isNotEmpty()) {
                val todayIdx = LocalDate.now().dayOfWeek.value % 7
                val today = schedule.value.getOrNull(todayIdx)
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDD5))) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Today's Goal", color = Color(0xFF5D4037))
                        Text(today?.task ?: "No task for today.", color = Color(0xFF5D4037))
                    }
                }
            }
        }
    }
}
