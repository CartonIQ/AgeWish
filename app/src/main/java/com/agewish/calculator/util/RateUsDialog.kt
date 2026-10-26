package com.agewish.calculator.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.agewish.calculator.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object RateUsDialog {

    private const val PREFS_NAME = "agewish_prefs"
    private const val KEY_PDF_COUNT = "pdf_generated_count"
    private const val KEY_HAS_RATED = "has_rated_app"
    private const val KEY_NEVER_SHOW_RATE = "never_show_rate"

    fun incrementPdfCountAndCheck(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hasRated = prefs.getBoolean(KEY_HAS_RATED, false)
        val neverShow = prefs.getBoolean(KEY_NEVER_SHOW_RATE, false)
        if (hasRated || neverShow) return

        val count = prefs.getInt(KEY_PDF_COUNT, 0) + 1
        prefs.edit().putInt(KEY_PDF_COUNT, count).apply()

        if (count == 3) {
            show(context)
        }
    }

    fun show(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.rate_dialog_title)
            .setMessage(R.string.rate_dialog_message)
            .setPositiveButton(R.string.btn_rate_now) { dialog, _ ->
                prefs.edit().putBoolean(KEY_HAS_RATED, true).apply()
                dialog.dismiss()
                openPlayStore(context)
            }
            .setNegativeButton(R.string.btn_rate_later) { dialog, _ ->
                dialog.dismiss()
            }
            .setNeutralButton(R.string.btn_rate_no_thanks) { dialog, _ ->
                prefs.edit().putBoolean(KEY_NEVER_SHOW_RATE, true).apply()
                dialog.dismiss()
            }
            .show()
    }

    private fun openPlayStore(context: Context) {
        val packageName = context.packageName
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            )
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }
}
