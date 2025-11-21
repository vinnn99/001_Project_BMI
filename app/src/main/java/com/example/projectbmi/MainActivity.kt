package com.example.projectbmi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.projectbmi.ui.theme.ProjectBMITheme
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.projectbmi.ui.screens.OnboardingScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase and sign-in anonymously so we can store user profiles in Firestore
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            // ignore if already initialized
        }
    // Attempt anonymous sign-in and then run migration from local DataStore to Firestore if needed
        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(androidx.lifecycle.Lifecycle.State.CREATED) {
            val auth = Firebase.auth
            try {
                if (auth.currentUser == null) {
                    // await sign-in so we can get UID reliably
                    val result = try {
                        val signInResult = auth.signInAnonymously().await()
                        android.widget.Toast.makeText(
                            this@MainActivity,
                            "Firebase connected successfully! UID: ${signInResult.user?.uid}",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        signInResult
                    } catch (fe: com.google.firebase.FirebaseException) {
                        // Common case: CONFIGURATION_NOT_FOUND when Firebase Auth not configured in console
                        android.util.Log.e("MainActivity", "FirebaseException during anonymous sign-in", fe)
                        val msg = fe.message ?: "Firebase configuration error"
                        // Show a friendly toast to guide the developer/user
                        android.widget.Toast.makeText(
                            this@MainActivity,
                            "Firebase sign-in failed: $msg. Check google-services.json and enable Anonymous Auth in Firebase Console.",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        null
                    }

                    val uid = result?.user?.uid
                    if (!uid.isNullOrBlank()) {
                        // Firestore-only mode: no local DataStore migration required
                    }
                } else {
                    // already signed in; ensure migration in case sign-in happened earlier
                    val uid = auth.currentUser?.uid
                    if (!uid.isNullOrBlank()) {
                        // Firestore-only mode: no local DataStore migration required
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("MainActivity", "Firebase init/signin/migration error", e)
            }
            }
        }
        setContent {
            ProjectBMITheme {
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
        composable("onboarding") { OnboardingScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("calculator") { BMICalculatorScreen(navController) }
        composable("result/{bmi}/{category}/{gender}") { backStackEntry ->
            val bmi = backStackEntry.arguments?.getString("bmi") ?: "0.0"
            val category = backStackEntry.arguments?.getString("category") ?: "Unknown"
            val gender = backStackEntry.arguments?.getString("gender") ?: "Male"
            ResultScreen(navController, bmi, category, gender)
        }
        composable("about") {
            AboutScreen(navController)
        }
        composable("history") {
            HistoryScreen(navController)
        }
        composable(
            route = "chat/{context}",
            arguments = listOf(navArgument("context") { type = NavType.StringType })
        ) { backStackEntry ->
            val context = backStackEntry.arguments?.getString("context") ?: ""
            ChatScreen(navController, initialContext = context)
        }
    }
}
