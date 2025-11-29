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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyQuestScreen(navController: NavController) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("bmi_prefs", Context.MODE_PRIVATE)

    val loading = remember { mutableStateOf(false) }
    val schedule = remember { mutableStateOf<List<QuestTask>>(emptyList()) }
    val completed = remember { mutableStateOf(mutableSetOf<Int>()) }
    val scope = rememberCoroutineScope()

    // Load saved schedule and completed flags on first composition
    LaunchedEffect(Unit) {
        loading.value = true
        try {
            val json = prefs.getString("weekly_schedule_json", "") ?: ""
            schedule.value = if (json.isBlank()) {
                // fallback to old string format
                val saved = prefs.getString("weekly_schedule", "") ?: ""
                if (saved.isBlank()) emptyList() else saved.split("||").mapIndexed { idx, s ->
                    val days = listOf("SUN","MON","TUE","WED","THU","FRI","SAT")
                    QuestTask(day = days.getOrNull(idx) ?: "", task = s)
                }
            } else {
                try {
                    Gson().fromJson(json, Array<QuestTask>::class.java).toList()
                } catch (e: Exception) {
                    emptyList()
                }
            }
            val compStr = prefs.getString("daily_completed", "") ?: ""
            if (compStr.isNotBlank()) {
                completed.value.clear()
                compStr.split(",").mapNotNull { it.toIntOrNull() }.forEach { completed.value.add(it) }
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
            prefs.edit().putString("daily_completed", completed.value.joinToString(",")).apply()
        } catch (_: Exception) {}
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
            .fillMaxSize()
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
                    val days = listOf("SUN", "MON", "TUE", "WED", "THU", "FRI", "SAT")
                    if (schedule.value.isEmpty()) {
                        Text("No schedule yet. Tap Generate to create a personalized weekly plan.", color = Color(0xFF555555))
                    } else {
                        schedule.value.forEachIndexed { idx, task ->
                            val isDone = completed.value.contains(idx)
                            Row(modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    if (isDone) completed.value.remove(idx) else completed.value.add(idx)
                                    persistCompleted()
                                },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(days.getOrNull(idx) ?: "-", modifier = Modifier.size(44.dp), color = Color(0xFF444444))
                                Checkbox(checked = isDone, onCheckedChange = { checked ->
                                    if (checked) completed.value.add(idx) else completed.value.remove(idx)
                                    persistCompleted()
                                })
                                Spacer(modifier = Modifier.size(8.dp))
                                    Column(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 12.dp)) {
                                    Text(task.task, color = Color(0xFF333333),
                                        textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    // Generate using last-known BMI/category/gender if available
                    scope.launch {
                        loading.value = true
                        try {
                            val bmi = prefs.getString("last_bmi", "0.0")?.toFloatOrNull() ?: 0f
                            val category = prefs.getString("last_category", "") ?: ""
                            val gender = prefs.getString("last_gender", "Male") ?: "Male"
                                    val generatedStrings = AIRepository.generateWeeklySchedule(bmi, category, gender)
                                    val days = listOf("SUN","MON","TUE","WED","THU","FRI","SAT")
                                    val generated = generatedStrings.mapIndexed { idx, s -> QuestTask(day = days.getOrNull(idx) ?: "", task = s) }
                                    schedule.value = generated
                                    persistSchedule(generated)
                            completed.value.clear()
                            persistCompleted()
                        } catch (_: Exception) {}
                        loading.value = false
                    }
                }) {
                    Text("Generate")
                }

                Button(onClick = {
                    // Clear saved schedule and completions
                        scope.launch {
                        schedule.value = emptyList()
                        completed.value.clear()
                        try { prefs.edit().remove("weekly_schedule_json").remove("daily_completed").apply() } catch (_: Exception) {}
                    }
                }) {
                    Text("Clear")
                }
            }

            Spacer(modifier = Modifier.padding(12.dp))

            // Quick today's task preview
            val todayIdx = LocalDate.now().dayOfWeek.value % 7
            val today = schedule.value.getOrNull(todayIdx)
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEDD5))) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Today's Goal", color = Color(0xFF5D4037))
                    Text(today?.task ?: "No task for today. Generate schedule to get started.", color = Color(0xFF5D4037))
                }
            }
        }
    }
}
