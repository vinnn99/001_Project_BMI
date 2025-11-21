package com.example.projectbmi.model

data class UserProfile(
    val fitnessGoals: Set<FitnessGoal> = emptySet(),
    val exerciseFrequency: ExerciseFrequency? = null,
    val dietPattern: DietPattern? = null,
    val sleepDuration: SleepDuration? = null,
    val weightManagementChallenges: Set<WeightManagementChallenge> = emptySet()
)

enum class FitnessGoal {
    LOSE_WEIGHT,
    GAIN_WEIGHT,
    MAINTAIN_WEIGHT,
    CHECK_BMI;

    fun label(): String = when(this) {
        LOSE_WEIGHT -> "Lose weight"
        GAIN_WEIGHT -> "Gain weight"
        MAINTAIN_WEIGHT -> "Maintain ideal weight"
        CHECK_BMI -> "Check BMI status"
    }
}

enum class ExerciseFrequency {
    NEVER,
    ONE_TO_TWO,
    THREE_TO_FIVE,
    DAILY;

    fun label(): String = when(this) {
        NEVER -> "Never"
        ONE_TO_TWO -> "1–2 times"
        THREE_TO_FIVE -> "3–5 times"
        DAILY -> "Daily"
    }
}

enum class DietPattern {
    IRREGULAR,
    REGULAR_OVEREATING,
    BALANCED,
    SPECIFIC_DIET;

    fun label(): String = when(this) {
        IRREGULAR -> "Irregular"
        REGULAR_OVEREATING -> "Regular but often overeating"
        BALANCED -> "Regular and balanced"
        SPECIFIC_DIET -> "Following a specific diet"
    }
}

enum class SleepDuration {
    LESS_THAN_FIVE,
    FIVE_TO_SEVEN,
    SEVEN_TO_NINE,
    MORE_THAN_NINE;

    fun label(): String = when(this) {
        LESS_THAN_FIVE -> "< 5 hours"
        FIVE_TO_SEVEN -> "5–7 hours"
        SEVEN_TO_NINE -> "7–9 hours"
        MORE_THAN_NINE -> "> 9 hours"
    }
}

enum class WeightManagementChallenge {
    EXERCISE_TIME,
    DIET_PATTERN,
    MOTIVATION,
    BUSY_SCHEDULE,
    OTHER;

    fun label(): String = when(this) {
        EXERCISE_TIME -> "Finding time to exercise"
        DIET_PATTERN -> "Diet pattern"
        MOTIVATION -> "Lack of motivation"
        BUSY_SCHEDULE -> "Busy schedule"
        OTHER -> "Other"
    }
}