package com.agewish.calculator.pdf

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.agewish.calculator.R
import java.io.File

object PdfSharer {

    private const val FILE_PROVIDER_AUTHORITY = "com.agewish.calculator.fileprovider"

    fun sharePdf(context: Context, pdfFile: File, title: String = context.getString(R.string.share_pdf_title)) {
        if (!pdfFile.exists()) return

        val uri = FileProvider.getUriForFile(
            context,
            FILE_PROVIDER_AUTHORITY,
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }
}
