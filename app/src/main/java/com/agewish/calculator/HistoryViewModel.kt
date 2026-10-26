package com.agewish.calculator

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agewish.calculator.data.HistoryEntity
import com.agewish.calculator.data.HistoryRepository
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: HistoryRepository) : ViewModel() {

    val allReports: LiveData<List<HistoryEntity>> = repository.allReports

    fun deleteReport(report: HistoryEntity) {
        viewModelScope.launch {
            repository.deleteReport(report)
        }
    }
}
