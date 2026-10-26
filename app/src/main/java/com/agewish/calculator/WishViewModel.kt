package com.agewish.calculator

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agewish.calculator.data.HistoryEntity
import com.agewish.calculator.data.HistoryRepository
import com.agewish.calculator.pdf.WishCardGenerator
import com.agewish.calculator.util.RateUsDialog
import com.agewish.calculator.util.ThemePreset
import com.agewish.calculator.util.WishTemplates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class WishViewModel : ViewModel() {

    private val _selectedOccasion = MutableLiveData<String>("Birthday")
    val selectedOccasion: LiveData<String> = _selectedOccasion

    private val _recipientName = MutableLiveData<String>("")
    val recipientName: LiveData<String> = _recipientName

    private val _senderName = MutableLiveData<String>("")
    val senderName: LiveData<String> = _senderName

    private val _message = MutableLiveData<String>(WishTemplates.OCCASIONS[0].defaultGreeting)
    val message: LiveData<String> = _message

    private val _selectedTheme = MutableLiveData<ThemePreset>(WishTemplates.THEME_PRESETS[0])
    val selectedTheme: LiveData<ThemePreset> = _selectedTheme

    private val _lastGeneratedPdf = MutableLiveData<File?>()
    val lastGeneratedPdf: LiveData<File?> = _lastGeneratedPdf

    fun setOccasion(occasion: String) {
        _selectedOccasion.value = occasion
        val config = WishTemplates.getOccasionByName(occasion)
        _message.value = config.defaultGreeting
        if (config.defaultThemeIndex in WishTemplates.THEME_PRESETS.indices) {
            _selectedTheme.value = WishTemplates.THEME_PRESETS[config.defaultThemeIndex]
        }
    }

    fun setRecipientName(name: String) {
        _recipientName.value = name
    }

    fun setSenderName(name: String) {
        _senderName.value = name
    }

    fun setMessage(msg: String) {
        _message.value = msg
    }

    fun setTheme(theme: ThemePreset) {
        _selectedTheme.value = theme
    }

    fun generateCardPdf(
        context: Context,
        repository: HistoryRepository,
        onComplete: (File) -> Unit
    ) {
        val occ = _selectedOccasion.value ?: "Birthday"
        val recipient = _recipientName.value?.trim().orEmpty()
        val sender = _senderName.value?.trim()
        val msg = _message.value.orEmpty()
        val theme = _selectedTheme.value ?: WishTemplates.THEME_PRESETS[0]
        val emoji = WishTemplates.getOccasionByName(occ).emoji

        viewModelScope.launch {
            val file = withContext(Dispatchers.IO) {
                WishCardGenerator.generateWishCardPdf(
                    context = context,
                    occasion = occ,
                    emoji = emoji,
                    recipientName = recipient,
                    senderName = sender,
                    message = msg,
                    theme = theme
                )
            }
            _lastGeneratedPdf.value = file

            val summary = "$emoji $occ Card for $recipient"
            val entity = HistoryEntity(
                type = "WISH",
                summary = summary,
                filePath = file.absolutePath
            )
            repository.insertReport(entity)

            RateUsDialog.incrementPdfCountAndCheck(context)
            onComplete(file)
        }
    }
}
