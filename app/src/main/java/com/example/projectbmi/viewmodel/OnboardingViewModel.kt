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
        _state.value = _state.value.copy(
            userProfile = _state.value.userProfile.copy(fitnessGoals = goals)
        )
    }

    fun updateExerciseFrequency(frequency: ExerciseFrequency) {
        _state.value = _state.value.copy(
            userProfile = _state.value.userProfile.copy(exerciseFrequency = frequency)
        )
    }

    fun updateDietPattern(pattern: DietPattern) {
        _state.value = _state.value.copy(
            userProfile = _state.value.userProfile.copy(dietPattern = pattern)
        )
    }

    fun updateSleepDuration(duration: SleepDuration) {
        _state.value = _state.value.copy(
            userProfile = _state.value.userProfile.copy(sleepDuration = duration)
        )
    }

    fun updateChallenges(challenges: Set<WeightManagementChallenge>) {
        _state.value = _state.value.copy(
            userProfile = _state.value.userProfile.copy(weightManagementChallenges = challenges)
        )
    }

    fun navigateNext() {
        val currentPage = _state.value.currentPage
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
    }

    private fun markComplete() {
        _state.value = _state.value.copy(isComplete = true)
        saveUserProfile()
    }

    private fun saveUserProfile() {
        // TODO: Implement saving to DataStore
        viewModelScope.launch {
            // Save user profile
        }
    }
}