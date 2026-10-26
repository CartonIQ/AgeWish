package com.agewish.calculator

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agewish.calculator.data.HistoryEntity
import com.agewish.calculator.data.HistoryRepository
import com.agewish.calculator.pdf.PdfGenerator
import com.agewish.calculator.util.AgeCalculationResult
import com.agewish.calculator.util.DateUtils
import com.agewish.calculator.util.RateUsDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDate

class AgeViewModel : ViewModel() {

    private val _dob = MutableLiveData<LocalDate>()
    val dob: LiveData<LocalDate> = _dob

    private val _asOf = MutableLiveData<LocalDate>(LocalDate.now())
    val asOf: LiveData<LocalDate> = _asOf

    private val _anniversary = MutableLiveData<LocalDate?>()
    val anniversary: LiveData<LocalDate?> = _anniversary

    private val _result = MutableLiveData<AgeCalculationResult?>()
    val result: LiveData<AgeCalculationResult?> = _result

    private val _lastGeneratedPdf = MutableLiveData<File?>()
    val lastGeneratedPdf: LiveData<File?> = _lastGeneratedPdf

    fun setDob(date: LocalDate) {
        _dob.value = date
    }

    fun setAsOf(date: LocalDate) {
        _asOf.value = date
    }

    fun setAnniversary(date: LocalDate?) {
        _anniversary.value = date
    }

    fun calculate(): Boolean {
        val birthDate = _dob.value ?: return false
        val asOfDate = _asOf.value ?: LocalDate.now()

        return try {
            val calcResult = DateUtils.calculateAge(birthDate, asOfDate, _anniversary.value)
            _result.value = calcResult
            true
        } catch (_: Exception) {
            false
        }
    }

    fun generatePdf(
        context: Context,
        repository: HistoryRepository,
        onComplete: (File) -> Unit
    ) {
        val currentResult = _result.value ?: return
        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) {
                PdfGenerator.generateAgePdf(context, currentResult)
            }
            _lastGeneratedPdf.value = file

            // Save to Room DB history
            val summary = "Age: ${currentResult.years}y ${currentResult.months}m ${currentResult.days}d • DOB: ${currentResult.dob.format(DateUtils.DATE_FORMATTER)}"
            val entity = HistoryEntity(
                type = "AGE",
                summary = summary,
                filePath = file.absolutePath
            )
            repository.insertReport(entity)

            // Increment count for Rate Us dialog
            RateUsDialog.incrementPdfCountAndCheck(context)

            onComplete(file)
        }
    }
}
