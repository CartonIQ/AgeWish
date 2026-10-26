package com.agewish.calculator.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_reports ORDER BY created_at DESC")
    fun getAllReports(): LiveData<List<HistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: HistoryEntity): Long

    @Delete
    suspend fun deleteReport(report: HistoryEntity)

    @Query("DELETE FROM history_reports WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM history_reports")
    suspend fun getReportCount(): Int
}
