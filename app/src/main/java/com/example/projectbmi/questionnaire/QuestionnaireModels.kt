package com.example.projectbmi.questionnaire

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data model representing a questionnaire screen with question, options, and selection type.
 * Used to configure questionnaire screens without hardcoding UI logic.
 */
data class QuestionScreen(
    val question: String,
    val subtitle: String,
    val icon: ImageVector,
    val options: List<QuestionOption>,
    val isMultiSelect: Boolean = false
)

/**
 * Data model representing a single option in a questionnaire.
 * @param text Display text shown to user
 * @param icon Icon representing the option
 * @param value Internal value used for state management (defaults to text)
 */
data class QuestionOption(
    val text: String,
    val icon: ImageVector,
    val value: String = text
)

/**
 * Centralized data source for all questionnaire screens.
 * Configuration-driven approach: add new questions by editing data, not code.
 */
object QuestionnaireData {
    val screens = listOf(
        // Screen 1: Fitness Goal
        QuestionScreen(
            question = "What's your main fitness goal?",
            subtitle = "This helps us create the perfect workout plan for you",
            icon = Icons.Outlined.TrackChanges,
            options = listOf(
                QuestionOption("Lose Weight", Icons.Outlined.TrendingDown, "weight-loss"),
                QuestionOption("Build Muscle", Icons.Outlined.FitnessCenter, "build"),
                QuestionOption("Maintain Fitness", Icons.Outlined.Balance, "maintain")
            )
        ),
        
        // Screen 2: Workout Intensity
        QuestionScreen(
            question = "What intensity level do you prefer?",
            subtitle = "Match your workout intensity to your lifestyle",
            icon = Icons.Outlined.Speed,
            options = listOf(
                QuestionOption("Low (Light activity)", Icons.Outlined.DirectionsWalk, "low"),
                QuestionOption("Medium (Moderate)", Icons.Outlined.DirectionsRun, "medium"),
                QuestionOption("High (Intense)", Icons.Outlined.Bolt, "high")
            )
        ),
        
        // Screen 3: Days Available
        QuestionScreen(
            question = "How many days can you workout per week?",
            subtitle = "We'll create a schedule that fits your availability",
            icon = Icons.Outlined.Event,
            options = listOf(
                QuestionOption("3-4 days", Icons.Outlined.Looks3, "3-4"),
                QuestionOption("5-6 days", Icons.Outlined.Looks5, "5-6"),
                QuestionOption("7 days (daily)", Icons.Outlined.Today, "7")
            )
        ),
        
        // Screen 4: Experience Level
        QuestionScreen(
            question = "What's your exercise experience level?",
            subtitle = "This helps us adjust workout difficulty appropriately",
            icon = Icons.Outlined.School,
            options = listOf(
                QuestionOption("Beginner", Icons.Outlined.EmojiPeople, "beginner"),
                QuestionOption("Intermediate", Icons.Outlined.DirectionsBike, "intermediate"),
                QuestionOption("Advanced", Icons.Outlined.SportsScore, "advanced")
            )
        ),
        
        // Screen 5: Focus Area (Multi-select)
        QuestionScreen(
            question = "What do you want to focus on?",
            subtitle = "Select all areas you want to prioritize in your training",
            icon = Icons.Outlined.Adjust,
            isMultiSelect = true,
            options = listOf(
                QuestionOption("Cardio", Icons.Outlined.DirectionsRun, "cardio"),
                QuestionOption("Strength", Icons.Outlined.FitnessCenter, "strength"),
                QuestionOption("Flexibility", Icons.Outlined.SelfImprovement, "flexibility"),
                QuestionOption("Mixed/Balanced", Icons.Outlined.Balance, "mixed")
            )
        )
    )
    
    /**
     * Get filtered fitness goal options based on BMI category.
     * Smart filter ensures recommendations are appropriate for user's weight status.
     */
    fun getFilteredGoalOptions(bmiCategory: String): List<QuestionOption> {
        val allGoals = screens[0].options // First screen has goal options
        
        return when (bmiCategory.lowercase()) {
            "underweight", "kurus" -> {
                // Underweight: focus on building muscle or maintaining
                allGoals.filter { it.value in listOf("build", "maintain") }
            }
            "normal", "normal weight" -> {
                // Normal: show all options
                allGoals
            }
            "overweight", "gemuk" -> {
                // Overweight: focus on losing weight or maintaining
                allGoals.filter { it.value in listOf("weight-loss", "maintain") }
            }
            "obese", "obesitas" -> {
                // Obese: prioritize weight loss
                allGoals.filter { it.value == "weight-loss" }
            }
            else -> {
                // Unknown/normal: show all options
                allGoals
            }
        }
    }
}
