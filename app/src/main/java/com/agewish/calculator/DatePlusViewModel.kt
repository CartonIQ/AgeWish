package com.agewish.calculator

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agewish.calculator.data.HistoryEntity
import com.agewish.calculator.data.HistoryRepository
import com.agewish.calculator.pdf.PdfGenerator
import com.agewish.calculator.util.DatePlusResult
import com.agewish.calculator.util.DateUtils
import com.agewish.calculator.util.RateUsDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate

class DatePlusViewModel : ViewModel() {

    private val _baseDate = MutableLiveData<LocalDate>(LocalDate.now())
    val baseDate: LiveData<LocalDate> = _baseDate

    private val _amount = MutableLiveData<Long>(30L)
    val amount: LiveData<Long> = _amount

    private val _isMonthsMode = MutableLiveData<Boolean>(false)
    val isMonthsMode: LiveData<Boolean> = _isMonthsMode

    private val _result = MutableLiveData<DatePlusResult?>()
    val result: LiveData<DatePlusResult?> = _result

    private val _lastGeneratedPdf = MutableLiveData<File?>()
    val lastGeneratedPdf: LiveData<File?> = _lastGeneratedPdf

    fun setBaseDate(date: LocalDate) {
        _baseDate.value = date
    }

    fun setAmount(amt: Long) {
        _amount.value = amt
    }

    fun setIsMonthsMode(monthsMode: Boolean) {
        _isMonthsMode.value = monthsMode
    }

    fun calculate(): Boolean {
        val base = _baseDate.value ?: LocalDate.now()
        val amt = _amount.value ?: 0L
        val monthsMode = _isMonthsMode.value ?: false

        val plusResult = DateUtils.calculateDatePlus(base, amt, monthsMode)
        _result.value = plusResult
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
                PdfGenerator.generateDatePlusPdf(context, currentResult)
            }
            _lastGeneratedPdf.value = file

            val sign = if (currentResult.amount >= 0) "+${currentResult.amount}" else "${currentResult.amount}"
            val unit = if (currentResult.isMonthsMode) "m" else "d"
            val summary = "Date $sign$unit → ${currentResult.resultDate.format(DateUtils.DATE_FORMATTER)} (${currentResult.dayOfWeek})"
            val entity = HistoryEntity(
                type = "DATE_PLUS",
                summary = summary,
                filePath = file.absolutePath
            )
            repository.insertReport(entity)

            RateUsDialog.incrementPdfCountAndCheck(context)
            onComplete(file)
        }
    }
}
