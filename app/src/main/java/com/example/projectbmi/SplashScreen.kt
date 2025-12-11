package com.example.projectbmi

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import java.net.URLEncoder

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        delay(2000) // tampil 2 detik
        
        // Check if user has previous BMI data
        val prefs = context.getSharedPreferences("bmi_prefs", android.content.Context.MODE_PRIVATE)
        val lastBmi = prefs.getString("last_bmi", null)
        val lastCategory = prefs.getString("last_category", null)
        val lastGender = prefs.getString("last_gender", null)
        
        android.util.Log.d("SplashScreen", "lastBmi=$lastBmi, lastCategory=$lastCategory, lastGender=$lastGender")
        
        // If all BMI data exists, go directly to Result screen
        if (!lastBmi.isNullOrEmpty() && !lastCategory.isNullOrEmpty() && !lastGender.isNullOrEmpty()) {
            android.util.Log.d("SplashScreen", "Going to result screen")
            val encodedCategory = URLEncoder.encode(lastCategory, "UTF-8")
            navController.navigate("result/$lastBmi/$encodedCategory/$lastGender") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            // Otherwise, go to BMI calculator
            android.util.Log.d("SplashScreen", "Going to calculator screen")
                navController.navigate("calculator?quick=false") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("BMI Mobile App", style = MaterialTheme.typography.headlineMedium)
    }
}