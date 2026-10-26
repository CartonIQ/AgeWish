package com.agewish.calculator

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agewish.calculator.data.HistoryEntity
import com.agewish.calculator.data.HistoryRepository
import com.agewish.calculator.pdf.PdfGenerator
import com.agewish.calculator.util.DateDifferenceResult
import com.agewish.calculator.util.DateUtils
import com.agewish.calculator.util.RateUsDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate

class DateDiffViewModel : ViewModel() {

    private val _startDate = MutableLiveData<LocalDate>(LocalDate.now().minusYears(1))
    val startDate: LiveData<LocalDate> = _startDate

    private val _endDate = MutableLiveData<LocalDate>(LocalDate.now())
    val endDate: LiveData<LocalDate> = _endDate

    private val _includeEndDay = MutableLiveData<Boolean>(false)
    val includeEndDay: LiveData<Boolean> = _includeEndDay

    private val _countWorkingDays = MutableLiveData<Boolean>(false)
    val countWorkingDays: LiveData<Boolean> = _countWorkingDays

    private val _result = MutableLiveData<DateDifferenceResult?>()
    val result: LiveData<DateDifferenceResult?> = _result

    private val _lastGeneratedPdf = MutableLiveData<File?>()
    val lastGeneratedPdf: LiveData<File?> = _lastGeneratedPdf

    fun setStartDate(date: LocalDate) {
        _startDate.value = date
    }

    fun setEndDate(date: LocalDate) {
        _endDate.value = date
    }

    fun setIncludeEndDay(include: Boolean) {
        _includeEndDay.value = include
    }

    fun setCountWorkingDays(workingOnly: Boolean) {
        _countWorkingDays.value = workingOnly
    }

    fun calculate(): Boolean {
        val start = _startDate.value ?: return false
        val end = _endDate.value ?: return false

        if (start.isAfter(end)) return false

        val diffResult = DateUtils.calculateDateDifference(
            start = start,
            end = end,
            includeEndDay = _includeEndDay.value ?: false,
            countWorkingDays = _countWorkingDays.value ?: false
        )
        _result.value = diffResult
        return true
    }

    fun generatePdf(
        context: Context,
        repository: HistoryRepository,
        onComplete: (File) -> Unit
    ) {
        val currentResult = _result.value ?: return
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) {
                PdfGenerator.generateDateDiffPdf(context, currentResult)
            }
            _lastGeneratedPdf.value = file

            val summary = "Diff: ${currentResult.years}y ${currentResult.months}m ${currentResult.days}d (${currentResult.totalDays} days)"
            val entity = HistoryEntity(
                type = "DATE_DIFF",
                summary = summary,
                filePath = file.absolutePath
            )
            repository.insertReport(entity)

            RateUsDialog.incrementPdfCountAndCheck(context)
            onComplete(file)
        }
    }
}
