package com.example.projectbmi.model

/**
 * Represents intensity recommendation based on user's age and selected intensity.
 * Helps suggest optimal intensity while respecting user's choice.
 */
data class IntensityRecommendation(
    val userSelected: String,           // What user selected (low, medium, high)
    val recommended: String,            // What's recommended for their age (low, medium, high)
    val userAge: Int,                   // User's age
    val shouldShowDialog: Boolean,      // Whether recommendation differs from selection
    val warningMessage: String = "",    // Warning message to display
    val explanation: String = ""        // Why this is recommended
) {
    fun needsConfirmation(): Boolean = shouldShowDialog
}

/**
 * Result of intensity confirmation - what user decided to do
 */
data class IntensityConfirmation(
    val finalIntensity: String,         // Final intensity user chose
    val wasRecommendationAccepted: Boolean  // Whether they accepted the recommendation
)
