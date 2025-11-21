package com.example.projectbmi

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectbmi.model.BMIRecord
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun HistoryScreen(navController: androidx.navigation.NavController, vm: HistoryViewModel = viewModel()) {
    val list = vm.history.collectAsState().value

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("History", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(list) { item ->
                HistoryRow(item)
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
