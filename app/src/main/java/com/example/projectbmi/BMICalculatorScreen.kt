package com.example.projectbmi

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectbmi.model.BMIRecord
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BMICalculatorScreen(navController: NavController, vm: BMIViewModel = viewModel()) {
    var step by remember { mutableStateOf(0) }

    val gender by vm.gender.collectAsState()
    val age by vm.age.collectAsState()
    val height by vm.height.collectAsState()
    val weight by vm.weight.collectAsState()
    // History VM to persist full records (height/weight)
    val historyVm: HistoryViewModel = viewModel()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Height", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        content = { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Stepper
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val filled = index <= step
                        Card(
                            shape = CircleShape,
                            colors = CardDefaults.cardColors(
                                containerColor = if (filled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.size(18.dp)
                        ) {}
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                when (step) {
                    0 -> {
                        Text("Select Gender", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            val selectedColor = MaterialTheme.colorScheme.tertiary
                            val unselectedBg = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)

                            // Male button
                            if (gender == "Male") {
                                Button(
                                    onClick = { vm.setGender("Male") },
                                    colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
                                ) { Text("Male", color = MaterialTheme.colorScheme.onTertiary) }
                            } else {
                                OutlinedButton(
                                    onClick = { vm.setGender("Male") },
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = unselectedBg)
                                ) { Text("Male") }
                            }

                            // Female button
                            if (gender == "Female") {
                                Button(
                                    onClick = { vm.setGender("Female") },
                                    colors = ButtonDefaults.buttonColors(containerColor = selectedColor)
                                ) { Text("Female", color = MaterialTheme.colorScheme.onTertiary) }
                            } else {
                                OutlinedButton(
                                    onClick = { vm.setGender("Female") },
                                    colors = ButtonDefaults.outlinedButtonColors(containerColor = unselectedBg)
                                ) { Text("Female") }
                            }
                        }
                    }
                    1 -> {
                        Text("What is your age?", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (age > 1) vm.setAge(age - 1) }) { Icon(Icons.Default.ArrowBack, contentDescription = "age-") }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(age.toString(), fontSize = 32.sp, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(12.dp))
                            IconButton(onClick = { if (age < 120) vm.setAge(age + 1) }) { Icon(Icons.Default.ArrowForward, contentDescription = "age+") }
                        }
                    }
                    2 -> {
                        Text("What is your height?", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(18.dp))
                        Image(
                            painter = painterResource(id = com.example.projectbmi.R.drawable.person_placeholder),
                            contentDescription = "person",
                            modifier = Modifier.size(140.dp)
                        )
                        Spacer(modifier = Modifier.height(18.dp))

                        var heightInput by remember { mutableStateOf(height.toString()) }
                        var heightError by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = heightInput,
                            onValueChange = {
                                val filtered = it.filter { ch -> ch.isDigit() }
                                heightInput = filtered
                                val parsed = filtered.toIntOrNull()
                                if (parsed == null) {
                                    heightError = "Please enter a number"
                                } else if (parsed < 50 || parsed > 250) {
                                    heightError = "Height must be between 50-250 cm"
                                } else {
                                    heightError = ""
                                    vm.setHeight(parsed)
                                }
                            },
                            label = { Text("Height (cm)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            
                        )
                        if (heightError.isNotEmpty()) {
                            Text(heightError, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                    3 -> {
                        Text("What is your weight?", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(18.dp))

                        var weightInput by remember { mutableStateOf(String.format("%.1f", weight)) }
                        var weightError by remember { mutableStateOf("") }
                        OutlinedTextField(
                            value = weightInput,
                            onValueChange = {
                                val filtered = it.filter { ch -> ch.isDigit() || ch == '.' }
                                weightInput = filtered
                                val parsed = filtered.toFloatOrNull()
                                if (parsed == null) {
                                    weightError = "Please enter a number"
                                } else if (parsed < 20f || parsed > 300f) {
                                    weightError = "Weight must be between 20-300 kg"
                                } else {
                                    weightError = ""
                                    vm.setWeight(parsed)
                                }
                            },
                            label = { Text("Weight (kg)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            
                        )
                        if (weightError.isNotEmpty()) {
                            Text(weightError, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    OutlinedButton(onClick = { if (step > 0) step -= 1 }) { Text("BACK") }
                    Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary), onClick = {
                        var canAdvance = true
                        // Simple validation: if on height or weight step, ensure no error text
                        if (step == 2) {
                            // read local validation state by checking vm.height range
                            val h = vm.height.value
                            if (h < 50 || h > 250) canAdvance = false
                        }
                        if (step == 3) {
                            val w = vm.weight.value
                            if (w < 20f || w > 300f) canAdvance = false
                        }

                        if (canAdvance) {
                            if (step < 3) step += 1 else {
                                val (bmi, category) = vm.calculateBmi()
                                // Save full record including height and weight before navigating
                                val rec = BMIRecord(
                                    timestamp = System.currentTimeMillis(),
                                    bmi = bmi,
                                    category = category,
                                    gender = vm.gender.value,
                                    heightCm = vm.height.value,
                                    weightKg = vm.weight.value
                                )
                                historyVm.saveRecord(rec)
                                navController.navigate("result/${"%.1f".format(bmi)}/$category/${vm.gender.value}")
                            }
                        }
                    }) { Text(if (step < 3) "NEXT" else "FINISH") }
                }
            }
        }
    )
}