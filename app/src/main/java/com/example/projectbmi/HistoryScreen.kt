package com.example.projectbmi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectbmi.model.BMIRecord
import java.text.SimpleDateFormat
import java.util.*
import android.util.Log

@Composable
fun HistoryScreen(navController: androidx.navigation.NavController, vm: HistoryViewModel = viewModel()) {
    Log.d("HistoryScreen", "Rendering history screen")
    val list = vm.history.collectAsState().value
    Log.d("HistoryScreen", "History loaded: ${list.size} records")

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("History", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))
        
        if (list.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        "No history yet",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Start by calculating your BMI to see your history here",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(list) { item ->
                    HistoryRow(item)
                }
            }
        }
    }
}

@Composable
fun HistoryRow(item: BMIRecord) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                val sdf = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.getDefault())
                Text(sdf.format(Date(item.timestamp)), style = MaterialTheme.typography.bodySmall)
                Text("BMI ${String.format("%.1f", item.bmi)} - ${item.category}", style = MaterialTheme.typography.bodyLarge)
                Text("${item.gender} • ${item.heightCm} cm • ${String.format("%.1f", item.weightKg)} kg", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
