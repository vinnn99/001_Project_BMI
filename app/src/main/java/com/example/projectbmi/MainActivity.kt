package com.example.projectbmi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                BMIMobileApp()
            }
        }
    }
}

@Composable
fun BMIMobileApp() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("calculator") { BMICalculatorScreen(navController) }
        composable("result/{bmi}/{category}") { backStackEntry ->
            val bmi = backStackEntry.arguments?.getString("bmi") ?: "0.0"
            val category = backStackEntry.arguments?.getString("category") ?: "Unknown"
            ResultScreen(navController, bmi, category)
        }
        composable("about") {
            AboutScreen(navController)
        }
    }
}
