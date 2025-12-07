package com.example.projectbmi.model

data class DailyQuestParams(
    val bmiCategory: String,
    val goal: String,
    val intensity: String,
    val timeAvailable: Int,  // Duration per session (30, 45, 60 minutes)
    val daysPerWeek: Int = 7,  // Number of workout days per week (3-4, 5-6, or 7)
    val focusArea: String,
    val healthStatus: String = "healthy",
    val exerciseHistory: String = "beginner",
    val dietaryPreference: String = "omnivore"
)
