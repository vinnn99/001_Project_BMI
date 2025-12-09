package com.example.projectbmi.model

import com.google.firebase.Timestamp

data class StreakData(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastCompletedDate: Timestamp? = null,
    val totalCompleted: Int = 0,
    val thisMonthCompleted: Int = 0,
    val updatedAt: Timestamp? = null
)

data class QuestHistoryDay(
    val date: String = "", // "YYYY-MM-DD"
    val dayOfWeek: String = "",
    val quests: List<QuestHistoryItem> = emptyList()
)

data class QuestHistoryItem(
    val questId: String = "",
    val questName: String = "",
    val completedAt: Timestamp? = null,
    val completed: Boolean = false
)

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(
        val streakData: StreakData,
        val historyDays: List<QuestHistoryDay>
    ) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
    object Empty : HistoryUiState()
}
