package com.example.projectbmi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectbmi.data.HistoryRepository
import com.example.projectbmi.model.BMIRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.util.Log

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = HistoryRepository()

    val history = repo.historyFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun saveRecord(record: BMIRecord) {
        viewModelScope.launch {
            try {
                Log.d("HistoryViewModel", "Saving record: $record")
                repo.addRecord(record)
                Log.d("HistoryViewModel", "Record saved successfully")
            } catch (e: Exception) {
                Log.e("HistoryViewModel", "Failed to save record to Firestore", e)
                // Don't rethrow — allow app to continue even if save fails
                // (offline mode or permission issue)
            }
        }
    }
}
