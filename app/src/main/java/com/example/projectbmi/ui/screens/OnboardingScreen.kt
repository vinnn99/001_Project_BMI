package com.example.projectbmi.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import androidx.navigation.NavController
import com.example.projectbmi.model.*
import com.example.projectbmi.ui.components.*
import com.example.projectbmi.viewmodel.OnboardingPage
import com.example.projectbmi.viewmodel.OnboardingViewModel

@Composable
fun OnboardingScreen(
    navController: NavController,
    viewModel: OnboardingViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) {
            // Save profile to Firestore (app now uses Firestore as single source-of-truth)
            val uid = Firebase.auth.currentUser?.uid
            if (!uid.isNullOrBlank()) {
                // build Firestore DTO
                val dto = com.example.projectbmi.repository.UserProfileDto(
                    fitnessGoals = state.userProfile.fitnessGoals.map { it.toString() },
                    exerciseFrequency = state.userProfile.exerciseFrequency?.toString(),
                    dietPattern = state.userProfile.dietPattern?.toString(),
                    sleepDuration = state.userProfile.sleepDuration?.toString(),
                    weightManagementChallenges = state.userProfile.weightManagementChallenges.map { it.toString() }
                )
                
                // Save profile before navigating to ensure it completes
                scope.launch {
                    try {
                        com.example.projectbmi.repository.FirestoreProfileRepository.saveProfile(uid, dto)
                        android.widget.Toast.makeText(
                            context,
                            "Profile saved to Firestore.",
                            android.widget.Toast.LENGTH_SHORT
                        ).show()
                        
                        // Only navigate after successful save
                        navController.navigate("home") {
                            popUpTo(0) { inclusive = true }
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("OnboardingScreen", "Failed to save profile to Firestore", e)
                        android.widget.Toast.makeText(
                            context,
                            "Failed to save profile: ${e.message}",
                            android.widget.Toast.LENGTH_LONG
                        ).show()
                        // Don't navigate if save failed
                    }
                }
            } else {
                // No uid available (rare if MainActivity signs in); inform user
                android.widget.Toast.makeText(
                    context,
                    "Unable to save profile: not signed in. Please restart the app to sign in.",
                    android.widget.Toast.LENGTH_LONG
                ).show()
                // Don't navigate if not signed in
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when (state.currentPage) {
            is OnboardingPage.FitnessGoals -> {
                MultiChoiceQuestion(
                    title = "What is your primary goal for using this app?",
                    options = FitnessGoal.values().map { it.label() },
                    selectedOptions = state.userProfile.fitnessGoals.map { it.label() }.toSet(),
                    onOptionSelected = { selected ->
                        val goals = FitnessGoal.values().filter { 
                            selected.contains(it.label()) 
                        }.toSet()
                        viewModel.updateFitnessGoals(goals)
                    }
                )
                OnboardingNextButton(
                    enabled = state.userProfile.fitnessGoals.isNotEmpty()
                ) {
                    viewModel.navigateNext()
                }
            }

            is OnboardingPage.ExerciseFrequency -> {
                SingleChoiceQuestion(
                    title = "How often do you exercise per week?",
                    options = ExerciseFrequency.values().map { it.label() },
                    selectedOption = state.userProfile.exerciseFrequency?.label(),
                    onOptionSelected = { selected ->
                        val frequency = ExerciseFrequency.values().first { 
                            it.label() == selected 
                        }
                        viewModel.updateExerciseFrequency(frequency)
                    }
                )
                OnboardingNextButton(
                    enabled = state.userProfile.exerciseFrequency != null
                ) {
                    viewModel.navigateNext()
                }
            }

            is OnboardingPage.DietPattern -> {
                SingleChoiceQuestion(
                    title = "What is your typical eating pattern?",
                    options = DietPattern.values().map { it.label() },
                    selectedOption = state.userProfile.dietPattern?.label(),
                    onOptionSelected = { selected ->
                        val pattern = DietPattern.values().first { 
                            it.label() == selected 
                        }
                        viewModel.updateDietPattern(pattern)
                    }
                )
                OnboardingNextButton(
                    enabled = state.userProfile.dietPattern != null
                ) {
                    viewModel.navigateNext()
                }
            }

            is OnboardingPage.SleepDuration -> {
                SingleChoiceQuestion(
                    title = "How many hours do you usually sleep per night?",
                    options = SleepDuration.values().map { it.label() },
                    selectedOption = state.userProfile.sleepDuration?.label(),
                    onOptionSelected = { selected ->
                        val duration = SleepDuration.values().first { 
                            it.label() == selected 
                        }
                        viewModel.updateSleepDuration(duration)
                    }
                )
                OnboardingNextButton(
                    enabled = state.userProfile.sleepDuration != null
                ) {
                    viewModel.navigateNext()
                }
            }

            is OnboardingPage.Challenges -> {
                MultiChoiceQuestion(
                    title = "What do you find most challenging about maintaining your ideal weight?",
                    options = WeightManagementChallenge.values().map { it.label() },
                    selectedOptions = state.userProfile.weightManagementChallenges.map { it.label() }.toSet(),
                    onOptionSelected = { selected ->
                        val challenges = WeightManagementChallenge.values().filter { 
                            selected.contains(it.label()) 
                        }.toSet()
                        viewModel.updateChallenges(challenges)
                    }
                )
                OnboardingNextButton(
                    enabled = state.userProfile.weightManagementChallenges.isNotEmpty()
                ) {
                    viewModel.navigateNext()
                }
            }
        }
    }
}