package com.example.projectbmi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = { navController.navigate("calculator") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hitung BMI")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("about") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Tentang Aplikasi")
        }
    }
}