package com.agewish.calculator.data

import androidx.lifecycle.LiveData
import java.io.File

class HistoryRepository(private val historyDao: HistoryDao) {
    val allReports: LiveData<List<HistoryEntity>> = historyDao.getAllReports()

    suspend fun insertReport(report: HistoryEntity): Long {
        return historyDao.insertReport(report)
    }

    suspend fun deleteReport(report: HistoryEntity) {
        try {
            val file = File(report.filePath)
            if (file.exists()) {
                file.delete()
            }
        } catch (_: Exception) {
            // Ignore file deletion errors
        }
        historyDao.deleteReport(report)
    }

    suspend fun getReportCount(): Int {
        return historyDao.getReportCount()
    }
}
