package com.example.projectbmi

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectbmi.data.HistoryRepository
import com.example.projectbmi.model.BMIRecord
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repo = HistoryRepository()

    val history = repo.historyFlow().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun saveRecord(record: BMIRecord) {
        viewModelScope.launch {
            repo.addRecord(record)
        }
    }
}
