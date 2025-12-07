package com.example.projectbmi

// Shared lightweight state for Ask AI / discovery flow
data class UserDiscoveryState(
    val bmiCategory: String = "normal",
    val primaryGoal: String? = null,  // Step 1: goal (weight-loss, maintain, build)
    val exerciseIntensity: String? = null,  // Step 2: intensity (low, medium, high)
    val daysAvailable: String? = null,  // Step 3: days available (3-4 days, 5-6 days, 7 days)
    val experienceLevel: String? = null,  // Step 4: experience (beginner, intermediate, advanced)
    val focusArea: String? = null,  // Step 5: focus area (cardio, strength, flexibility, mixed)
    val exerciseFrequency: String? = null,
    val eatingPattern: String? = null,
    val sleepHours: String? = null,
    val challenges: List<String> = emptyList(),
    val currentStep: Int = 1,
    val isLoading: Boolean = false
)
