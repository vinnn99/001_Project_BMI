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
}
