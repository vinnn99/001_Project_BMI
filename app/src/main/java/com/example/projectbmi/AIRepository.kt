package com.example.projectbmi

import com.example.projectbmi.model.BMIRecord

/**
 * Simple on-device rule-based tips generator used as a stub for AI integration.
 * Keeps everything local and private; later can be replaced by a cloud LLM call.
 */
class AIRepository {
    fun generateQuickTips(bmi: Float, category: String, gender: String, age: Int = 0, history: List<BMIRecord> = emptyList()): List<String> {
        val tips = mutableListOf<String>()
        if (category.lowercase().contains("normal")) {
            tips.add("Great job — maintain a balanced diet and regular exercise 3x/week.")
            tips.add("Include strength training 2x/week to preserve muscle mass.")
        } else if (category.lowercase().contains("underweight") || category.lowercase().contains("kurus")) {
            tips.add("Consider small frequent meals and add healthy calorie-dense foods (nuts, avocado).")
            tips.add("Consult a nutritionist if unintended weight loss continues.")
        } else if (category.lowercase().contains("overweight") || category.lowercase().contains("gemuk")) {
            tips.add("Start with daily 20–30 minutes of brisk walking and reduce sugary drinks.")
            tips.add("Replace one high-calorie snack with fruit or yogurt.")
        } else {
            tips.add("Aim for a mix of cardio and strength training and watch portion sizes.")
            tips.add("Track food intake for a week to find easy places to reduce calories.")
        }

        // small personalization
        if (age in 60..150) {
            tips.add("For older adults, prioritize balance exercises and low-impact cardio.")
        }
        if (gender.lowercase().contains("f")) {
            tips.add("Ensure adequate calcium and vitamin D intake for bone health.")
        }

        return tips
    }

    /**
     * Generate a simple 7-day schedule (one short task per day) tailored to the BMI category and gender.
     * This is intentionally lightweight and deterministic so it can run locally without network.
     */
    fun generateWeeklySchedule(bmi: Float, category: String, gender: String, age: Int = 0): List<String> {
        val lower = category.lowercase()
        val baseTasks = when {
            lower.contains("underweight") || lower.contains("kurus") -> listOf(
                "Eat 3 balanced meals + 2 nutritious snacks",
                "Add a protein- and calorie-dense smoothie",
                "Strength training: 20–30 min (bodyweight)",
                "Include healthy fats (nuts, avocado) in meals",
                "Try a calorie-dense breakfast (eggs, oats)",
                "Light walk + focus on portion size increases",
                "Prepare meal plan with extra calories"
            )
            lower.contains("overweight") || lower.contains("gemuk") -> listOf(
                "30 min brisk walking or cycling",
                "Swap soda for water or unsweetened tea",
                "Include a vegetable-rich dinner",
                "Strength training: 20–30 min (light weights)",
                "Limit sugary snacks today",
                "Try a guided home cardio session (20 min)",
                "Plan healthy meals for the week"
            )
            lower.contains("obese") -> listOf(
                "20 min low-impact walking or pool exercise",
                "Choose a high-protein breakfast",
                "Replace one meal with a large salad",
                "Short resistance band session: 15–20 min",
                "Avoid sugary drinks today",
                "Practice mindful eating at each meal",
                "Schedule a short consult/check-in with a provider"
            )
            else -> listOf(
                "30 min activity (brisk walk)",
                "Maintain balanced meals",
                "Strength training: 2 sets of basic moves",
                "Hydration focus: 8 glasses",
                "Try a new healthy recipe",
                "Track food intake for today",
                "Rest and light mobility work"
            )
        }

        // Small personalization tweaks
        val genderHint = if (gender.lowercase().startsWith("f")) " (prioritize bone health)" else ""

        return baseTasks.mapIndexed { idx, task ->
            // Add gentle personalization without exposing PII
            if (idx == 0) "$task$genderHint" else task
        }
    }
}
