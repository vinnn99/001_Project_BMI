package com.example.projectbmi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.projectbmi.AlarmHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RemindersSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    var remindersEnabled by remember { mutableStateOf(true) }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Workout Reminders") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Toggle reminders on/off
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Enable Workout Reminders", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Get reminded daily at 8:00 AM about your workout",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = remindersEnabled,
                            onCheckedChange = { enabled ->
                                remindersEnabled = enabled
                                if (enabled) {
                                    AlarmHelper.scheduleWorkoutReminder(context)
                                } else {
                                    AlarmHelper.cancelWorkoutReminder(context)
                                }
                            }
                        )
                    }
                }
            }

            // Schedule info card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(end = 12.dp)
                                .size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text("Reminder Schedule", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("• Time: 8:00 AM daily", style = MaterialTheme.typography.bodySmall)
                            Text("• Trigger: If you haven't completed today's workout", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // How it works
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 12.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Column {
                            Text(
                                "How It Works",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Once you generate a workout schedule, reminders will automatically trigger at 8:00 AM on days you have scheduled workouts. If you complete your workout and mark it as done, you won't receive a reminder that day.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
