package com.example.projectbmi.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectbmi.model.HistoryUiState
import com.example.projectbmi.model.QuestHistoryDay
import com.example.projectbmi.model.StreakData
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class HistoryViewModel(private val userId: String) : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val tag = "HistoryViewModel"

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState

    init {
        loadData()
    }

    private fun loadData() {
        _uiState.value = HistoryUiState.Loading
        loadStreakAndHistory()
    }

    private fun loadStreakAndHistory() {
        var streakData: StreakData? = null
        var historyDays: List<QuestHistoryDay>? = null

        // Load streak
        db.collection("users")
            .document(userId)
            .collection("streakData")
            .document("current")
            .addSnapshotListener { doc, error ->
                if (error != null) {
                    Log.e(tag, "Error loading streak", error)
                    _uiState.value = HistoryUiState.Error(error.message ?: "Error loading streak")
                    return@addSnapshotListener
                }

                streakData = if (doc?.exists() == true) {
                    doc.toObject(StreakData::class.java) ?: StreakData()
                } else {
                    StreakData()
                }

                updateUiState(streakData, historyDays)
            }

        // Load history
        val thirtyDaysAgo = LocalDate.now().minusDays(30).toString()
        db.collection("users")
            .document(userId)
            .collection("dailyQuestHistory")
            .whereGreaterThanOrEqualTo("date", thirtyDaysAgo)
            .orderBy("date", Query.Direction.DESCENDING)
            .limit(30)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(tag, "Error loading history", error)
                    _uiState.value = HistoryUiState.Error(error.message ?: "Error loading history")
                    return@addSnapshotListener
                }

                historyDays = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(QuestHistoryDay::class.java)
                } ?: emptyList()

                updateUiState(streakData, historyDays)
            }
    }

    private fun updateUiState(streak: StreakData?, history: List<QuestHistoryDay>?) {
        if (streak != null && history != null) {
            _uiState.value = if (history.isEmpty()) {
                HistoryUiState.Empty
            } else {
                HistoryUiState.Success(streak, history)
            }
        }
    }

    fun logQuestCompletion(questName: String, questId: String = "") {
        viewModelScope.launch {
            try {
                val today = LocalDate.now().toString()
                val dayOfWeek = LocalDate.now().dayOfWeek.toString()

                val questItem = mapOf(
                    "questId" to (questId.ifEmpty { questName }),
                    "questName" to questName,
                    "completedAt" to Timestamp.now(),
                    "completed" to true
                )

                db.collection("users")
                    .document(userId)
                    .collection("dailyQuestHistory")
                    .document(today)
                    .get()
                    .addOnSuccessListener { doc ->
                        if (doc.exists()) {
                            val quests = (doc.get("quests") as? List<Map<String, Any>>)?.toMutableList() ?: mutableListOf()
                            quests.add(questItem)

                            db.collection("users")
                                .document(userId)
                                .collection("dailyQuestHistory")
                                .document(today)
                                .update("quests", quests)
                                .addOnSuccessListener {
                                    Log.d(tag, "Quest logged: $questName")
                                    updateStreakData()
                                }
                        } else {
                            val historyDay = mapOf(
                                "date" to today,
                                "dayOfWeek" to dayOfWeek,
                                "quests" to listOf(questItem)
                            )

                            db.collection("users")
                                .document(userId)
                                .collection("dailyQuestHistory")
                                .document(today)
                                .set(historyDay)
                                .addOnSuccessListener {
                                    Log.d(tag, "New history day: $today")
                                    updateStreakData()
                                }
                        }
                    }
            } catch (e: Exception) {
                Log.e(tag, "Error logging quest", e)
            }
        }
    }

    private fun updateStreakData() {
        try {
            val today = LocalDate.now()
            val yesterday = today.minusDays(1).toString()

            db.collection("users")
                .document(userId)
                .collection("streakData")
                .document("current")
                .get()
                .addOnSuccessListener { doc ->
                    val current = if (doc.exists()) {
                        doc.toObject(StreakData::class.java) ?: StreakData()
                    } else {
                        StreakData()
                    }

                    val newStreak = if (current.lastCompletedDate != null) {
                        val lastDate = current.lastCompletedDate!!.toDate()
                        val lastLocalDate = java.time.LocalDateTime
                            .ofInstant(lastDate.toInstant(), java.time.ZoneId.systemDefault())
                            .toLocalDate()

                        when {
                            lastLocalDate == yesterday.let { LocalDate.parse(it) } -> current.currentStreak + 1
                            lastLocalDate == today -> current.currentStreak
                            else -> 1
                        }
                    } else {
                        1
                    }

                    val updated = StreakData(
                        currentStreak = newStreak,
                        longestStreak = maxOf(newStreak, current.longestStreak),
                        lastCompletedDate = Timestamp.now(),
                        totalCompleted = current.totalCompleted + 1,
                        thisMonthCompleted = current.thisMonthCompleted + 1,
                        updatedAt = Timestamp.now()
                    )

                    db.collection("users")
                        .document(userId)
                        .collection("streakData")
                        .document("current")
                        .set(updated)
                        .addOnSuccessListener {
                            Log.d(tag, "Streak updated: ${updated.currentStreak} days")
                        }
                }
        } catch (e: Exception) {
            Log.e(tag, "Error updating streak", e)
        }
    }
}
