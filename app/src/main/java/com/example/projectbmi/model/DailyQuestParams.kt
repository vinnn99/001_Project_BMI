package com.example.projectbmi.model

data class DailyQuestParams(
    val bmiCategory: String,
    val goal: String,
    val intensity: String,
    val timeAvailable: Int,
    val focusArea: String,
    val healthStatus: String = "healthy",
    val exerciseHistory: String = "beginner",
    val dietaryPreference: String = "omnivore"
)
