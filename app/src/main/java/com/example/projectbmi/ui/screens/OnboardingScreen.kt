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
            // Navigate immediately to the BMI calculator so user flow isn't blocked.
            // Save the profile asynchronously in the background; navigation should not depend on it.
            navController.navigate("calculator") {
                popUpTo(0) { inclusive = true }
            }

            // Attempt to save profile in background if signed in.
            val uid = Firebase.auth.currentUser?.uid
            if (!uid.isNullOrBlank()) {
                val dto = com.example.projectbmi.repository.UserProfileDto(
                    fitnessGoals = state.userProfile.fitnessGoals.map { it.toString() },
                    exerciseFrequency = state.userProfile.exerciseFrequency?.toString(),
                    dietPattern = state.userProfile.dietPattern?.toString(),
                    sleepDuration = state.userProfile.sleepDuration?.toString(),
                    weightManagementChallenges = state.userProfile.weightManagementChallenges.map { it.toString() }
                )
                scope.launch {
                    try {
                        com.example.projectbmi.repository.FirestoreProfileRepository.saveProfile(uid, dto)
                        android.widget.Toast.makeText(context, "Profile saved.", android.widget.Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        android.util.Log.w("OnboardingScreen", "Failed to save profile to Firestore", e)
                    }
                }
            } else {
                // Not signed in; saving will be skipped but navigation proceeds.
                android.util.Log.w("OnboardingScreen", "User not signed in; skipping profile save")
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
                // Local selection to ensure immediate UI response and reliable enablement
                var localSelected by remember {
                    mutableStateOf(state.userProfile.fitnessGoals.map { it.label() }.toMutableSet<String>())
                }

                MultiChoiceQuestion(
                    title = "What is your primary goal for using this app?",
                    options = FitnessGoal.values().map { it.label() },
                    selectedOptions = localSelected,
                    onOptionSelected = { selected ->
                        localSelected = selected.toMutableSet()
                        val goals = FitnessGoal.values().filter {
                            selected.contains(it.label())
                        }.toSet()
                        viewModel.updateFitnessGoals(goals)
                    }
                )
                OnboardingNextButton(
                    enabled = localSelected.isNotEmpty()
                ) {
                    viewModel.navigateNext()
                }
            }

            is OnboardingPage.ExerciseFrequency -> {
                var localSelected by remember { mutableStateOf(state.userProfile.exerciseFrequency?.label()) }

                SingleChoiceQuestion(
                    title = "How often do you exercise per week?",
                    options = ExerciseFrequency.values().map { it.label() },
                    selectedOption = localSelected,
                    onOptionSelected = { selected ->
                        localSelected = selected
                        val frequency = ExerciseFrequency.values().first {
                            it.label() == selected
                        }
                        viewModel.updateExerciseFrequency(frequency)
                    }
                )
                OnboardingNextButton(
                    enabled = localSelected != null
                ) {
                    viewModel.navigateNext()
                }
            }

            is OnboardingPage.DietPattern -> {
                var localSelected by remember { mutableStateOf(state.userProfile.dietPattern?.label()) }

                SingleChoiceQuestion(
                    title = "What is your typical eating pattern?",
                    options = DietPattern.values().map { it.label() },
                    selectedOption = localSelected,
                    onOptionSelected = { selected ->
                        localSelected = selected
                        val pattern = DietPattern.values().first {
                            it.label() == selected
                        }
                        viewModel.updateDietPattern(pattern)
                    }
                )
                OnboardingNextButton(
                    enabled = localSelected != null
                ) {
                    viewModel.navigateNext()
                }
            }

            is OnboardingPage.SleepDuration -> {
                var localSelected by remember { mutableStateOf(state.userProfile.sleepDuration?.label()) }

                SingleChoiceQuestion(
                    title = "How many hours do you usually sleep per night?",
                    options = SleepDuration.values().map { it.label() },
                    selectedOption = localSelected,
                    onOptionSelected = { selected ->
                        localSelected = selected
                        val duration = SleepDuration.values().first {
                            it.label() == selected
                        }
                        viewModel.updateSleepDuration(duration)
                    }
                )
                OnboardingNextButton(
                    enabled = localSelected != null
                ) {
                    viewModel.navigateNext()
                }
            }

            is OnboardingPage.Challenges -> {
                var localSelected by remember {
                    mutableStateOf(state.userProfile.weightManagementChallenges.map { it.label() }.toMutableSet<String>())
                }

                MultiChoiceQuestion(
                    title = "What do you find most challenging about maintaining your ideal weight?",
                    options = WeightManagementChallenge.values().map { it.label() },
                    selectedOptions = localSelected,
                    onOptionSelected = { selected ->
                        localSelected = selected.toMutableSet()
                        val challenges = WeightManagementChallenge.values().filter {
                            selected.contains(it.label())
                        }.toSet()
                        viewModel.updateChallenges(challenges)
                    }
                )
                OnboardingNextButton(
                    enabled = localSelected.isNotEmpty()
                ) {
                    viewModel.navigateNext()
                }
            }
        }
    }
}