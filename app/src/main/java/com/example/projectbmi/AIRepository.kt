package com.example.projectbmi

import com.example.projectbmi.model.BMIRecord
import com.example.projectbmi.model.QuestTask
import com.example.projectbmi.model.DailyQuestParams

/**
 * Simple on-device rule-based tips generator used as a stub for AI integration.
 * Keeps everything local and private; later can be replaced by a cloud LLM call.
 */
object AIRepository {
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

    /**
     * Generate a personalized 7-day weekly schedule based on detailed user discovery profile.
     * Returns a list of QuestTask objects with day, task, duration, intensity, and notes.
     */
    fun generateWeeklySchedule(params: DailyQuestParams): List<QuestTask> {
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        val schedule = mutableListOf<QuestTask>()

        val tasks = when {
            // Weight Loss Focus
            params.goal == "weight-loss" && params.intensity == "low" -> listOf(
                QuestTask("Monday", "20 min brisk walk", "cardio", 20, "low", "Burn ~100 cal"),
                QuestTask("Tuesday", "Track meals + water intake", "nutrition", 15, "low", "Use app to log food"),
                QuestTask("Wednesday", "15 min home stretching", "flexibility", 15, "low", "Improve mobility"),
                QuestTask("Thursday", "20 min light cycling", "cardio", 20, "low", "Steady pace"),
                QuestTask("Friday", "Meal prep for weekend", "nutrition", 30, "low", "Prepare healthy portions"),
                QuestTask("Saturday", "30 min leisurely walk", "cardio", 30, "low", "Outdoor activity"),
                QuestTask("Sunday", "Rest + hydration focus", "recovery", 10, "low", "Drink 2L water")
            )
            params.goal == "weight-loss" && params.intensity == "medium" -> listOf(
                QuestTask("Monday", "30 min cardio (walk/jog)", "cardio", 30, "medium", "Burn ~300 cal"),
                QuestTask("Tuesday", "Track meals + strength 20 min", "mixed", 40, "medium", "Light weights"),
                QuestTask("Wednesday", "30 min cardio session", "cardio", 30, "medium", "Steady-state"),
                QuestTask("Thursday", "Strength training 25 min", "strength", 25, "medium", "Upper body focus"),
                QuestTask("Friday", "Meal prep + light cardio", "mixed", 45, "medium", "30 min prep + 15 min walk"),
                QuestTask("Saturday", "45 min mixed activity", "mixed", 45, "medium", "Cardio + flexibility"),
                QuestTask("Sunday", "20 min stretching + nutrition planning", "recovery", 20, "low", "Plan next week")
            )
            params.goal == "weight-loss" && params.intensity == "high" -> listOf(
                QuestTask("Monday", "45 min HIIT cardio", "cardio", 45, "high", "Intervals + recovery"),
                QuestTask("Tuesday", "40 min strength training", "strength", 40, "high", "Full body circuit"),
                QuestTask("Wednesday", "45 min cardio (run/bike)", "cardio", 45, "high", "High intensity"),
                QuestTask("Thursday", "40 min strength + core", "strength", 40, "high", "Weights + abs"),
                QuestTask("Friday", "Meal prep + 20 min cardio", "mixed", 50, "high", "Prep + light activity"),
                QuestTask("Saturday", "60 min mixed workout", "mixed", 60, "high", "Cardio + strength"),
                QuestTask("Sunday", "Active recovery + meal plan", "recovery", 30, "low", "Yoga + planning")
            )

            // Maintain Focus
            params.goal == "maintain" && params.intensity == "low" -> listOf(
                QuestTask("Monday", "20 min walk", "cardio", 20, "low", "Light activity"),
                QuestTask("Tuesday", "Balanced meal check", "nutrition", 10, "low", "Review diet"),
                QuestTask("Wednesday", "15 min stretching", "flexibility", 15, "low", "Mobility work"),
                QuestTask("Thursday", "20 min casual activity", "cardio", 20, "low", "Flexible timing"),
                QuestTask("Friday", "Light meal planning", "nutrition", 15, "low", "Prep basics"),
                QuestTask("Saturday", "30 min leisure walk", "cardio", 30, "low", "Enjoy movement"),
                QuestTask("Sunday", "Rest + hydration", "recovery", 10, "low", "Recovery focus")
            )
            params.goal == "maintain" && params.intensity == "medium" -> listOf(
                QuestTask("Monday", "30 min cardio", "cardio", 30, "medium", "Walk or cycle"),
                QuestTask("Tuesday", "25 min strength", "strength", 25, "medium", "2-3 exercises"),
                QuestTask("Wednesday", "30 min cardio", "cardio", 30, "medium", "Running or swim"),
                QuestTask("Thursday", "25 min strength", "strength", 25, "medium", "Different muscles"),
                QuestTask("Friday", "Meal prep", "nutrition", 30, "low", "Prepare meals"),
                QuestTask("Saturday", "40 min mixed", "mixed", 40, "medium", "Cardio + strength"),
                QuestTask("Sunday", "Stretching + planning", "recovery", 20, "low", "Relax & plan")
            )
            params.goal == "maintain" && params.intensity == "high" -> listOf(
                QuestTask("Monday", "45 min cardio", "cardio", 45, "high", "Fast pace"),
                QuestTask("Tuesday", "40 min strength", "strength", 40, "high", "Full body"),
                QuestTask("Wednesday", "45 min cardio", "cardio", 45, "high", "Interval training"),
                QuestTask("Thursday", "40 min strength", "strength", 40, "high", "Heavy weights"),
                QuestTask("Friday", "30 min cardio", "cardio", 30, "medium", "Recovery pace"),
                QuestTask("Saturday", "60 min mixed", "mixed", 60, "high", "Complete workout"),
                QuestTask("Sunday", "Active recovery", "recovery", 30, "low", "Yoga or walk")
            )

            // Build Muscle Focus
            params.goal == "build" && params.intensity == "low" -> listOf(
                QuestTask("Monday", "20 min light strength", "strength", 20, "low", "Bodyweight basics"),
                QuestTask("Tuesday", "Track protein intake", "nutrition", 15, "low", "Aim for 1.6g/kg"),
                QuestTask("Wednesday", "20 min light strength", "strength", 20, "low", "Different exercises"),
                QuestTask("Thursday", "15 min core work", "strength", 15, "low", "Abs & stability"),
                QuestTask("Friday", "Meal prep (high protein)", "nutrition", 30, "low", "Protein-rich meals"),
                QuestTask("Saturday", "20 min light strength", "strength", 20, "low", "Gentle progression"),
                QuestTask("Sunday", "Rest + protein tracking", "recovery", 10, "low", "Recovery focus")
            )
            params.goal == "build" && params.intensity == "medium" -> listOf(
                QuestTask("Monday", "35 min strength (push)", "strength", 35, "medium", "Chest, shoulders, tris"),
                QuestTask("Tuesday", "Track high-protein meals", "nutrition", 15, "low", "1.6-2g protein/kg"),
                QuestTask("Wednesday", "35 min strength (pull)", "strength", 35, "medium", "Back, lats, bis"),
                QuestTask("Thursday", "30 min leg day", "strength", 30, "medium", "Quads, hamstrings"),
                QuestTask("Friday", "Meal prep + light cardio", "mixed", 40, "low", "20 min activity"),
                QuestTask("Saturday", "40 min strength (full)", "strength", 40, "medium", "Compound movements"),
                QuestTask("Sunday", "Rest + meal planning", "recovery", 15, "low", "Prep high-protein meals")
            )
            params.goal == "build" && params.intensity == "high" -> listOf(
                QuestTask("Monday", "45 min strength (push)", "strength", 45, "high", "Heavy compounds"),
                QuestTask("Tuesday", "Protein tracking + prep", "nutrition", 20, "low", "2g protein/kg goal"),
                QuestTask("Wednesday", "45 min strength (pull)", "strength", 45, "high", "Heavy lifts"),
                QuestTask("Thursday", "45 min leg day", "strength", 45, "high", "Squats, deadlifts"),
                QuestTask("Friday", "30 min light cardio", "cardio", 30, "low", "Active recovery"),
                QuestTask("Saturday", "60 min full strength", "strength", 60, "high", "All muscle groups"),
                QuestTask("Sunday", "Rest + nutrition review", "recovery", 20, "low", "Plan nutrition")
            )

            else -> listOf(
                QuestTask("Monday", "30 min activity", "cardio", 30, "medium", "Any movement"),
                QuestTask("Tuesday", "20 min strength", "strength", 20, "medium", "Light weights"),
                QuestTask("Wednesday", "25 min cardio", "cardio", 25, "medium", "Walk or cycle"),
                QuestTask("Thursday", "15 min stretching", "flexibility", 15, "low", "Mobility"),
                QuestTask("Friday", "Meal prep", "nutrition", 30, "low", "Prepare meals"),
                QuestTask("Saturday", "40 min mixed", "mixed", 40, "medium", "Varied activity"),
                QuestTask("Sunday", "Rest day", "recovery", 10, "low", "Relax")
            )
        }

        return tasks
    }
}
