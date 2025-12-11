package com.example.projectbmi

import android.os.Bundle
import android.content.Context
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
import kotlinx.coroutines.delay
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectbmi.HistoryViewModel
import com.example.projectbmi.model.QuestTask
import com.example.projectbmi.ui.screens.HistoryScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize notification channel for reminders
        NotificationHelper.createNotificationChannel(this)
        
        // Log notification setup
        android.util.Log.d("MainActivity", "Notification channel created")
        
        // Install a global uncaught-exception handler to capture crashes
        try {
            Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
                try {
                    android.util.Log.e("UncaughtException", "Uncaught exception in thread ${thread.name}", throwable)
                    // attempt to persist stacktrace to app-private file for later inspection
                    val crashFile = java.io.File(filesDir, "last_crash.log")
                    crashFile.appendText("Thread: ${thread.name}\n")
                    crashFile.appendText(android.util.Log.getStackTraceString(throwable) + "\n\n")
                } catch (_: Exception) {
                }
                // rethrow to let system handle termination
                android.os.Process.killProcess(android.os.Process.myPid())
                System.exit(2)
            }
        } catch (e: Exception) {
            android.util.Log.w("MainActivity", "Failed to install global exception handler", e)
        }
        // Initialize Firebase - DO NOT sign in anonymously, let BMIMobileApp handle auth
        try {
            FirebaseApp.initializeApp(this)
        } catch (e: Exception) {
            // ignore if already initialized
        }
        
        // Skip anonymous sign-in - it interferes with Google OAuth login flow
        // Firebase initialization is enough for Firestore access after user logs in with Google
        
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
    val auth = Firebase.auth
    
    // Get context untuk SharedPreferences flag
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = context.getSharedPreferences("app_auth_state", Context.MODE_PRIVATE)
    
    // State untuk track login
    val currentUser = remember { mutableStateOf<com.google.firebase.auth.FirebaseUser?>(null) }
    val isListenerReady = remember { mutableStateOf(false) }
    val isLoggingOut = remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // Cek SharedPreferences - jika belum login, force sign out & clear Firebase persistence
        val wasLoggedIn = prefs.getBoolean("wasLoggedIn", false)
        val appInitialized = prefs.getBoolean("app_initialized", false)
        
        android.util.Log.d("BMIMobileApp", "wasLoggedIn from prefs: $wasLoggedIn, appInitialized: $appInitialized, currentUser: ${auth.currentUser?.email}")
        
        // First time app launch - clear all user data
        if (!appInitialized) {
            android.util.Log.d("BMIMobileApp", "First app launch - clearing all user data")
            val bmiPrefs = context.getSharedPreferences("bmi_prefs", Context.MODE_PRIVATE)
            bmiPrefs.edit().clear().commit()
            
            val dailyQuestPrefs = context.getSharedPreferences("daily_quest_prefs", Context.MODE_PRIVATE)
            dailyQuestPrefs.edit().clear().commit()
            
            prefs.edit().putBoolean("app_initialized", true).commit()
        }
        
        if (!wasLoggedIn) {
            // Force sign out
            auth.signOut()
            
            // Delete Firebase cache files manually
            try {
                val cacheDir = java.io.File(context.filesDir.parent, "com.google.firebase.auth")
                if (cacheDir.exists()) {
                    cacheDir.deleteRecursively()
                    android.util.Log.d("BMIMobileApp", "Deleted Firebase cache dir")
                }
            } catch (e: Exception) {
                android.util.Log.e("BMIMobileApp", "Error deleting cache", e)
            }
            
            delay(500)
            android.util.Log.d("BMIMobileApp", "Force signed out and cleared cache")
        }
        
        // Register auth listener AFTER force signout
        val listener = auth.addAuthStateListener { authInstance ->
            currentUser.value = authInstance.currentUser
            
            // Update SharedPreferences based on auth state
            if (authInstance.currentUser != null) {
                prefs.edit().putBoolean("wasLoggedIn", true).apply()
                android.util.Log.d("BMIMobileApp", "Listener: Logged in: ${authInstance.currentUser?.email}")
            } else {
                prefs.edit().putBoolean("wasLoggedIn", false).apply()
                android.util.Log.d("BMIMobileApp", "Listener: Logged out or null")
            }
            
            isListenerReady.value = true
        }
    }
    
    // Tunggu listener siap
    if (!isListenerReady.value) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    
    // Route based on Firebase auth state
    val startDestination = if (currentUser.value != null) "splash" else "login"
    
    android.util.Log.d("BMIMobileApp", "Navigation: $startDestination, user: ${currentUser.value?.email}")
    
    NavHost(navController = navController, startDestination = startDestination) {
        composable("login") {
            com.example.projectbmi.ui.screens.LoginScreen(navController)
        }
        composable("splash") { SplashScreen(navController) }
        // `onboarding` route removed — app now navigates to calculator directly from splash
        composable(
            route = "calculator?quick={quick}",
            arguments = listOf(navArgument("quick") { type = NavType.BoolType; defaultValue = false })
        ) { backStackEntry ->
            val quick = backStackEntry.arguments?.getBoolean("quick") ?: false
            BMICalculatorScreen(navController, quickRecalc = quick)
        }
        composable("result/{bmi}/{category}/{gender}") { backStackEntry ->
            val bmi = backStackEntry.arguments?.getString("bmi") ?: "0.0"
            val encodedCategory = backStackEntry.arguments?.getString("category") ?: "Unknown"
            val category = java.net.URLDecoder.decode(encodedCategory, "UTF-8")
            val gender = backStackEntry.arguments?.getString("gender") ?: "Male"
            ResultScreen(navController, bmi, category, gender)
        }
        composable("history") {
            HistoryScreen(
                onBackClick = { navController.navigateUp() },
                onLogout = {
                    isLoggingOut.value = true
                    
                    // Clear ALL app data FIRST before sign out
                    val bmiPrefs = context.getSharedPreferences("bmi_prefs", Context.MODE_PRIVATE)
                    bmiPrefs.edit().clear().commit()
                    android.util.Log.d("Logout", "bmi_prefs cleared")
                    
                    val dailyQuestPrefs = context.getSharedPreferences("daily_quest_prefs", Context.MODE_PRIVATE)
                    dailyQuestPrefs.edit().clear().commit()
                    android.util.Log.d("Logout", "daily_quest_prefs cleared")
                    
                    prefs.edit().putBoolean("wasLoggedIn", false).commit()
                    android.util.Log.d("Logout", "wasLoggedIn set to false")
                    
                    // Verify cleared
                    val verifyBmi = bmiPrefs.getString("last_bmi", null)
                    android.util.Log.d("Logout", "Verification: last_bmi after clear = $verifyBmi")
                    
                    // Then sign out
                    Firebase.auth.signOut()
                    
                    android.util.Log.d("Logout", "Firebase signed out, navigating to login")
                    
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                    isLoggingOut.value = false
                }
            )
        }
        composable("dailyquest") {
            DailyQuestScreen(navController)
        }
        composable("askAI") {
            val context = androidx.compose.ui.platform.LocalContext.current
            // Get BMI category from SharedPreferences
            val prefs = context.getSharedPreferences("bmi_prefs", android.content.Context.MODE_PRIVATE)
            val bmiCategory = prefs.getString("last_category", "Normal") ?: "Normal"
            
            AskAIScreenClean(
                navController = navController,
                userBMICategory = bmiCategory,
                onSaveSchedule = { schedule ->
                    try {
                        val questPrefs = context.getSharedPreferences("daily_quest_prefs", android.content.Context.MODE_PRIVATE)
                        val gson = com.google.gson.Gson()
                        val json = gson.toJson(schedule)
                        questPrefs.edit().putString("weekly_schedule_json", json).apply()
                    } catch (e: Exception) {
                        android.util.Log.e("MainActivity", "Failed to save schedule", e)
                    }
                }
            )
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
