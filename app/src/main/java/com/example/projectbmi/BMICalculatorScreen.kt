package com.example.projectbmi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun BMICalculatorScreen(navController: NavController) {
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "BMI Calculator",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = weight,
            onValueChange = { weight = it },
            label = { Text("Berat Badan (kg)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Tinggi Badan (cm)") },
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(errorMessage, color = MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                val w = weight.toFloatOrNull()
                val h = height.toFloatOrNull()

                if (w != null && h != null && h > 0) {
                    val bmi = w / ((h / 100) * (h / 100))
                    val category = when {
                        bmi < 18.5 -> "Kurus"
                        bmi in 18.5..24.9 -> "Normal"
                        bmi in 25.0..29.9 -> "Gemuk"
                        else -> "Obesitas"
                    }
                    errorMessage = ""
                    navController.navigate("result/${"%.1f".format(bmi)}/$category")
                } else {
                    errorMessage = "Mohon isi berat dan tinggi dengan benar"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Hitung BMI")
        }
    }
}