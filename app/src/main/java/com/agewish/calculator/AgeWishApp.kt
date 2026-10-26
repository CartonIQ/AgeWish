package com.agewish.calculator

import android.app.Application
import com.agewish.calculator.ads.AdManager
import com.agewish.calculator.data.AppDatabase
import com.agewish.calculator.data.HistoryRepository

class AgeWishApp : Application() {

    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy { HistoryRepository(database.historyDao()) }

    override fun onCreate() {
        super.onCreate()
        AdManager.initialize(this)
    }
}
