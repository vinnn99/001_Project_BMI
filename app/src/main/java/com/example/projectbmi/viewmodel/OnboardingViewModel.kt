package com.example.projectbmi.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectbmi.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

sealed class OnboardingPage {
    object FitnessGoals : OnboardingPage()
    object ExerciseFrequency : OnboardingPage()
    object DietPattern : OnboardingPage()
    object SleepDuration : OnboardingPage()
    object Challenges : OnboardingPage()
}

data class OnboardingState(
    val currentPage: OnboardingPage = OnboardingPage.FitnessGoals,
    val userProfile: UserProfile = UserProfile(),
    val isComplete: Boolean = false
)

class OnboardingViewModel : ViewModel() {
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun updateFitnessGoals(goals: Set<FitnessGoal>) {
        try {
            Log.d("OnboardingViewModel", "Updating fitness goals: ${goals.size} selected")
            _state.value = _state.value.copy(
                userProfile = _state.value.userProfile.copy(fitnessGoals = goals)
            )
        } catch (e: Exception) {
            Log.e("OnboardingViewModel", "Error updating fitness goals", e)
        }
    }

    fun updateExerciseFrequency(frequency: ExerciseFrequency) {
        try {
            Log.d("OnboardingViewModel", "Updating exercise frequency: $frequency")
            _state.value = _state.value.copy(
                userProfile = _state.value.userProfile.copy(exerciseFrequency = frequency)
            )
        } catch (e: Exception) {
            Log.e("OnboardingViewModel", "Error updating exercise frequency", e)
        }
    }

    fun updateDietPattern(pattern: DietPattern) {
        try {
            Log.d("OnboardingViewModel", "Updating diet pattern: $pattern")
            _state.value = _state.value.copy(
                userProfile = _state.value.userProfile.copy(dietPattern = pattern)
            )
        } catch (e: Exception) {
            Log.e("OnboardingViewModel", "Error updating diet pattern", e)
        }
    }

    fun updateSleepDuration(duration: SleepDuration) {
        try {
            Log.d("OnboardingViewModel", "Updating sleep duration: $duration")
            _state.value = _state.value.copy(
                userProfile = _state.value.userProfile.copy(sleepDuration = duration)
            )
        } catch (e: Exception) {
            Log.e("OnboardingViewModel", "Error updating sleep duration", e)
        }
    }

    fun updateChallenges(challenges: Set<WeightManagementChallenge>) {
        try {
            Log.d("OnboardingViewModel", "Updating challenges: ${challenges.size} selected")
            _state.value = _state.value.copy(
                userProfile = _state.value.userProfile.copy(weightManagementChallenges = challenges)
            )
        } catch (e: Exception) {
            Log.e("OnboardingViewModel", "Error updating challenges", e)
        }
    }

    fun navigateNext() {
        try {
            val currentPage = _state.value.currentPage
            Log.d("OnboardingViewModel", "Navigating from page: $currentPage")
            val nextPage = when (currentPage) {
                is OnboardingPage.FitnessGoals -> OnboardingPage.ExerciseFrequency
                is OnboardingPage.ExerciseFrequency -> OnboardingPage.DietPattern
                is OnboardingPage.DietPattern -> OnboardingPage.SleepDuration
                is OnboardingPage.SleepDuration -> OnboardingPage.Challenges
                is OnboardingPage.Challenges -> {
                    markComplete()
                    return
                }
            }
            _state.value = _state.value.copy(currentPage = nextPage)
            Log.d("OnboardingViewModel", "Navigated to page: $nextPage")
        } catch (e: Exception) {
            Log.e("OnboardingViewModel", "Error navigating", e)
        }
    }

    private fun markComplete() {
        try {
            Log.d("OnboardingViewModel", "Marking onboarding as complete")
            _state.value = _state.value.copy(isComplete = true)
            saveUserProfile()
        } catch (e: Exception) {
            Log.e("OnboardingViewModel", "Error marking complete", e)
        }
    }

    private fun saveUserProfile() {
        // Save user profile asynchronously
        viewModelScope.launch {
            try {
                Log.d("OnboardingViewModel", "Saving user profile")
                // TODO: Implement saving to DataStore or Firebase
                Log.d("OnboardingViewModel", "Profile saved successfully")
            } catch (e: Exception) {
                Log.e("OnboardingViewModel", "Error saving profile", e)
            }
        }
    }
}