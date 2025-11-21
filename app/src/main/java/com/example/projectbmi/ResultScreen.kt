package com.example.projectbmi

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.projectbmi.model.BMIRecord
import java.util.*
import androidx.compose.foundation.clickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalContext
import com.google.firebase.ktx.Firebase
import com.google.firebase.auth.ktx.auth
import com.example.projectbmi.repository.FirestoreProfileRepository
import kotlinx.coroutines.flow.collect

@Composable
fun ResultScreen(
    navController: NavController,
    bmi: String,
    category: String,
    gender: String,
    historyVm: HistoryViewModel = viewModel(),
    coachVm: CoachViewModel = viewModel(),
    onboardingVm: com.example.projectbmi.viewmodel.OnboardingViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Your BMI Result", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(24.dp))

        // AI Coach Quick Tips area
        val tipsState = coachVm.tips.collectAsState()
        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("AI Coach", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                if (tipsState.value.isEmpty()) {
                    Text("Get personalized quick tips generated for you.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = {
                        val b = try { bmi.toFloat() } catch (e: Exception) { 0f }
                        // load quick tips using history (last 5 records)
                        val history = historyVm.history.value.take(5)
                        coachVm.loadQuickTips(b, category, gender, 0, history)
                    }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)) {
                        Text("Get quick tips")
                    }
                } else {
                    Column {
                        tipsState.value.forEach { tip ->
                            Text("• $tip", modifier = Modifier.padding(vertical = 4.dp))
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        // Prefer Firestore profile (if user signed in), otherwise fall back to DataStore
                        val ctxLocal = LocalContext.current
                        val uid = Firebase.auth.currentUser?.uid

                        val firestoreProfileText by androidx.compose.runtime.produceState(initialValue = "", uid) {
                            if (!uid.isNullOrBlank()) {
                                try {
                                    val dto = FirestoreProfileRepository.getProfileOnce(uid)
                                    android.util.Log.d("ResultScreen", "Fetched profile: $dto")
                                    if (dto != null) {
                                        val goals = if (dto.fitnessGoals.isNotEmpty()) dto.fitnessGoals.joinToString(", ") { name ->
                                            try { com.example.projectbmi.model.FitnessGoal.values().first { it.toString() == name }.label() } catch (e: Exception) { name }
                                        } else "No specific goals set"
                                        val exercise = dto.exerciseFrequency?.let { name -> try { com.example.projectbmi.model.ExerciseFrequency.values().first { it.toString() == name }.label() } catch (e: Exception) { "No exercise information" } } ?: "No exercise information"
                                        val diet = dto.dietPattern?.let { name -> try { com.example.projectbmi.model.DietPattern.values().first { it.toString() == name }.label() } catch (e: Exception) { "No diet information" } } ?: "No diet information"
                                        val sleep = dto.sleepDuration?.let { name -> try { com.example.projectbmi.model.SleepDuration.values().first { it.toString() == name }.label() } catch (e: Exception) { "No sleep information" } } ?: "No sleep information"
                                        val challenges = if (dto.weightManagementChallenges.isNotEmpty()) dto.weightManagementChallenges.joinToString(", ") { name -> try { com.example.projectbmi.model.WeightManagementChallenge.values().first { it.toString() == name }.label() } catch (e: Exception) { name } } else "No specific challenges"

                                        value = """
                                            Current BMI: $bmi ($category)
                                            Gender: $gender
                                            Fitness Goals: $goals
                                            Exercise Habits: $exercise
                                            Diet Pattern: $diet
                                            Sleep Pattern: $sleep
                                            Weight Management Challenges: $challenges
                                        """.trimIndent()
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("ResultScreen", "Error reading Firestore profile", e)
                                }
                            }
                        }

                        val profileText = if (!uid.isNullOrBlank() && firestoreProfileText.isNotBlank()) {
                            firestoreProfileText
                        } else {
                            // Firestore profile not available; show placeholder defaults
                            """
                                Current BMI: $bmi ($category)
                                Gender: $gender
                                Fitness Goals: No specific goals set
                                Exercise Habits: No exercise information
                                Diet Pattern: No diet information
                                Sleep Pattern: No sleep information
                                Weight Management Challenges: No specific challenges
                            """.trimIndent()
                        }

                        OutlinedButton(
                            onClick = {
                                val contextStr = if (profileText.isNotBlank()) profileText else "Current BMI: $bmi ($category); Gender: $gender"
                                navController.navigate("chat/${android.net.Uri.encode(contextStr)}")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Chat with AI Coach for more advice")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        // Provide an explicit way for users to complete or edit their profile
                        OutlinedButton(
                            onClick = {
                                // Navigate to onboarding so user can fill in missing profile fields
                                navController.navigate("onboarding")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Complete / Edit Profile")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("(Tips are generated locally. Chat requires internet connection.)", 
                             style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        val ctx = LocalContext.current
        // resource naming convention: person_<gender>_<category>
        val categoryKey = category.lowercase().let { cat ->
            when {
                cat.contains("underweight") -> "underweight"
                cat.contains("normal") -> "normal"
                cat.contains("overweight") -> "overweight"
                cat.contains("obese") -> "obese"
                else -> "normal" // fallback
            }
        }
        val genderKey = if (gender.lowercase().contains("f")) "female" else "male"
        val resName = "person_${genderKey}_${categoryKey}"
        println("Debug: Looking for resource: $resName") // Debug log
        val resId = ctx.resources.getIdentifier(resName, "drawable", ctx.packageName)
        println("Debug: Resource ID: $resId") // Debug log

        // Choose subtle background per category for better visual feedback
        val cardBg = when (categoryKey) {
            "underweight" -> androidx.compose.ui.graphics.Color(0xFFE8F3FF)
            "normal" -> androidx.compose.ui.graphics.Color(0xFFE8FFF0)
            "overweight" -> androidx.compose.ui.graphics.Color(0xFFFFF3E0)
            else -> androidx.compose.ui.graphics.Color(0xFFFFEBEE)
        }

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = cardBg)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Image container on the left with white frame
                val imageId = if (resId != 0) resId else com.example.projectbmi.R.drawable.person_placeholder
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(androidx.compose.ui.graphics.Color.White)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imageId),
                        contentDescription = "result_illustration",
                        contentScale = androidx.compose.ui.layout.ContentScale.Fit,
                        alignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Details right — take remaining space so layout is balanced
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                    Text(text = "BMI", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = bmi, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(text = category, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))



        Column(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    // include onboarding answers in the context
                    val profile = onboardingVm.state.value.userProfile
                    val goals = if (profile.fitnessGoals.isNotEmpty()) profile.fitnessGoals.joinToString(", ") { it.label() } else "No specific goals set"
                    val exercise = profile.exerciseFrequency?.label() ?: "No exercise information"
                    val diet = profile.dietPattern?.label() ?: "No diet information"
                    val sleep = profile.sleepDuration?.label() ?: "No sleep information"
                    val challenges = if (profile.weightManagementChallenges.isNotEmpty()) 
                        profile.weightManagementChallenges.joinToString(", ") { it.label() } 
                    else "No specific challenges"

                    val context = """
                        Current BMI: $bmi ($category)
                        Gender: $gender
                        Fitness Goals: $goals
                        Exercise Habits: $exercise
                        Diet Pattern: $diet
                        Sleep Pattern: $sleep
                        Weight Management Challenges: $challenges
                    """.trimIndent()
                    navController.navigate("chat/${android.net.Uri.encode(context)}")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
            ) {
                Text("Konsultasi dengan AI Coach")
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = { navController.navigate("home") {
                    popUpTo("home") { inclusive = true }
                } }, modifier = Modifier.weight(1f)) { Text("Home") }

                Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary), onClick = { navController.navigate("calculator") {
                    popUpTo("calculator") { inclusive = true }
                } }, modifier = Modifier.weight(1f)) { Text("Calculate Again") }
            }
        }
    }
}