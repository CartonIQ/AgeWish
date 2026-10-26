package com.agewish.calculator.pdf

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.agewish.calculator.util.AgeCalculationResult
import com.agewish.calculator.util.DateDifferenceResult
import com.agewish.calculator.util.DatePlusResult
import com.agewish.calculator.util.DateUtils
import com.agewish.calculator.util.ResultFormatter
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object PdfGenerator {

    private const val PAGE_WIDTH = 595 // Standard A4 width in PostScript points (72 DPI)
    private const val PAGE_HEIGHT = 842 // Standard A4 height in PostScript points (72 DPI)
    private const val MARGIN = 40f

    fun generateAgePdf(context: Context, result: AgeCalculationResult): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var y = MARGIN + 25f

        // Title Paint
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // Subtitle / Date Paint
        val subPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        // Section Header Paint
        val sectionPaint = Paint().apply {
            color = Color.rgb(0, 105, 92) // Teal accent
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        // Body Text Paint
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        // Bold Body Text Paint
        val boldBodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        // Header
        canvas.drawText("AgeWish Report – Age Calculation", MARGIN, y, titlePaint)
        y += 18f

        val nowFormatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a", Locale.getDefault()))
        canvas.drawText("Generated on: $nowFormatted", MARGIN, y, subPaint)
        y += 15f

        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 24f

        // Section: Inputs
        canvas.drawText("INPUT PARAMETERS", MARGIN, y, sectionPaint)
        y += 18f
        drawKeyValue(canvas, "Date of Birth:", result.dob.format(DateUtils.DATE_FORMATTER), MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f
        drawKeyValue(canvas, "Calculated As Of:", result.asOf.format(DateUtils.DATE_FORMATTER), MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f
        result.anniversaryResult?.let {
            drawKeyValue(canvas, "Anniversary Date:", it.anniversaryDate.format(DateUtils.DATE_FORMATTER), MARGIN, y, boldBodyPaint, bodyPaint)
            y += 18f
        }

        y += 10f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 24f

        // Section: Primary Age
        canvas.drawText("CALCULATED AGE & MILESTONES", MARGIN, y, sectionPaint)
        y += 22f
        val primaryAgeText = "Age: ${result.years} years, ${result.months} months, ${result.days} days"
        val highlightPaint = Paint(titlePaint).apply {
            textSize = 15f
            color = Color.rgb(0, 77, 64)
        }
        canvas.drawText(primaryAgeText, MARGIN, y, highlightPaint)
        y += 20f

        drawKeyValue(canvas, "Total Days Lived:", "${ResultFormatter.formatNumber(result.totalDays)} days", MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f
        drawKeyValue(canvas, "Total Weeks Lived:", "${ResultFormatter.formatNumber(result.totalWeeks)} weeks", MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f
        drawKeyValue(canvas, "Total Months Lived:", "${ResultFormatter.formatNumber(result.totalMonths)} months", MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f
        drawKeyValue(canvas, "Total Hours Lived:", "${ResultFormatter.formatNumber(result.totalHours)} hours", MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f
        drawKeyValue(canvas, "Total Minutes Lived:", "${ResultFormatter.formatNumber(result.totalMinutes)} minutes", MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f

        y += 10f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 24f

        // Section: Upcoming Events & Zodiac
        canvas.drawText("UPCOMING CELEBRATIONS & ASTROLOGY", MARGIN, y, sectionPaint)
        y += 18f
        if (result.isBirthdayToday) {
            drawKeyValue(canvas, "Birthday Status:", "TODAY is your Birthday! Happy Birthday! 🎂🎉", MARGIN, y, boldBodyPaint, boldBodyPaint)
        } else {
            drawKeyValue(canvas, "Next Birthday:", "In ${result.daysToNextBirthday} days (on ${result.nextBirthdayDate.format(DateUtils.DATE_FORMATTER)})", MARGIN, y, boldBodyPaint, bodyPaint)
        }
        y += 18f

        result.anniversaryResult?.let {
            if (it.isAnniversaryToday) {
                drawKeyValue(canvas, "Anniversary Status:", "TODAY is your Anniversary! Happy Anniversary! 💍❤️", MARGIN, y, boldBodyPaint, boldBodyPaint)
            } else {
                drawKeyValue(canvas, "Next Anniversary:", "In ${it.daysToNextAnniversary} days (on ${it.nextAnniversaryDate.format(DateUtils.DATE_FORMATTER)})", MARGIN, y, boldBodyPaint, bodyPaint)
            }
            y += 18f
        }

        drawKeyValue(canvas, "Western Zodiac Sign:", result.westernZodiac, MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f
        drawKeyValue(canvas, "Chinese Zodiac Sign:", result.chineseZodiac, MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f
        drawKeyValue(canvas, "Day of Week Born:", result.dayOfWeekBorn, MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f

        // Footer at bottom
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }
        canvas.drawLine(MARGIN, PAGE_HEIGHT - MARGIN - 20f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - MARGIN - 20f, linePaint)
        canvas.drawText("Generated by AgeWish – Calculate age. Send wishes.", MARGIN, PAGE_HEIGHT - MARGIN - 6f, footerPaint)

        document.finishPage(page)

        val outputFile = createOutputFile(context, "AgeReport_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return outputFile
    }

    fun generateDateDiffPdf(context: Context, result: DateDifferenceResult): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var y = MARGIN + 25f

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val sectionPaint = Paint().apply {
            color = Color.rgb(0, 105, 92)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val boldBodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        canvas.drawText("AgeWish Report – Date Difference", MARGIN, y, titlePaint)
        y += 18f

        val nowFormatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a", Locale.getDefault()))
        canvas.drawText("Generated on: $nowFormatted", MARGIN, y, subPaint)
        y += 15f

        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 24f

        canvas.drawText("INPUT DATES", MARGIN, y, sectionPaint)
        y += 18f
        drawKeyValue(canvas, "Start Date:", result.startDate.format(DateUtils.DATE_FORMATTER), MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f
        drawKeyValue(canvas, "End Date:", result.endDate.format(DateUtils.DATE_FORMATTER), MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f
        drawKeyValue(canvas, "Include End Day:", if (result.includeEndDay) "Yes (+1 day)" else "No", MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f

        y += 10f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 24f

        canvas.drawText("DIFFERENCE ANALYSIS", MARGIN, y, sectionPaint)
        y += 22f
        val diffText = "Difference: ${result.years} years, ${result.months} months, ${result.days} days"
        val highlightPaint = Paint(titlePaint).apply {
            textSize = 15f
            color = Color.rgb(0, 77, 64)
        }
        canvas.drawText(diffText, MARGIN, y, highlightPaint)
        y += 20f

        drawKeyValue(canvas, "Total Days:", "${ResultFormatter.formatNumber(result.totalDays)} days", MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f
        drawKeyValue(canvas, "Total Weeks:", "${ResultFormatter.formatNumber(result.totalWeeks)} weeks", MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f
        result.workingDays?.let {
            drawKeyValue(canvas, "Working Days:", "${ResultFormatter.formatNumber(it)} days (excluding Saturday & Sunday)", MARGIN, y, boldBodyPaint, bodyPaint)
            y += 18f
        }

        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }
        canvas.drawLine(MARGIN, PAGE_HEIGHT - MARGIN - 20f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - MARGIN - 20f, linePaint)
        canvas.drawText("Generated by AgeWish – Calculate age. Send wishes.", MARGIN, PAGE_HEIGHT - MARGIN - 6f, footerPaint)

        document.finishPage(page)

        val outputFile = createOutputFile(context, "DateDiffReport_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return outputFile
    }

    fun generateDatePlusPdf(context: Context, result: DatePlusResult): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        var y = MARGIN + 25f

        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val subPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val sectionPaint = Paint().apply {
            color = Color.rgb(0, 105, 92)
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val bodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }
        val boldBodyPaint = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        canvas.drawText("AgeWish Report – Date Math (± N)", MARGIN, y, titlePaint)
        y += 18f

        val nowFormatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy, hh:mm a", Locale.getDefault()))
        canvas.drawText("Generated on: $nowFormatted", MARGIN, y, subPaint)
        y += 15f

        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 24f

        canvas.drawText("CALCULATION PARAMETERS", MARGIN, y, sectionPaint)
        y += 18f
        drawKeyValue(canvas, "Base Date:", result.baseDate.format(DateUtils.DATE_FORMATTER), MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f
        val modeText = if (result.isMonthsMode) "Months" else "Days"
        val offsetSign = if (result.amount >= 0) "+${result.amount}" else "${result.amount}"
        drawKeyValue(canvas, "Offset Amount:", "$offsetSign $modeText", MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f

        y += 10f
        canvas.drawLine(MARGIN, y, PAGE_WIDTH - MARGIN, y, linePaint)
        y += 24f

        canvas.drawText("RESULT DATE", MARGIN, y, sectionPaint)
        y += 22f
        val resultText = "Result Date: ${result.resultDate.format(DateUtils.DATE_FORMATTER)} (${result.dayOfWeek})"
        val highlightPaint = Paint(titlePaint).apply {
            textSize = 15f
            color = Color.rgb(0, 77, 64)
        }
        canvas.drawText(resultText, MARGIN, y, highlightPaint)
        y += 20f

        drawKeyValue(canvas, "Full Date:", result.resultDate.format(DateUtils.DISPLAY_FORMATTER), MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f
        drawKeyValue(canvas, "Day of the Week:", result.dayOfWeek, MARGIN, y, boldBodyPaint, bodyPaint)
        y += 18f

        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
            isAntiAlias = true
        }
        canvas.drawLine(MARGIN, PAGE_HEIGHT - MARGIN - 20f, PAGE_WIDTH - MARGIN, PAGE_HEIGHT - MARGIN - 20f, linePaint)
        canvas.drawText("Generated by AgeWish – Calculate age. Send wishes.", MARGIN, PAGE_HEIGHT - MARGIN - 6f, footerPaint)

        document.finishPage(page)

        val outputFile = createOutputFile(context, "DatePlusReport_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return outputFile
    }

    private fun drawKeyValue(
        canvas: android.graphics.Canvas,
        key: String,
        value: String,
        x: Float,
        y: Float,
        keyPaint: Paint,
        valPaint: Paint
    ) {
        canvas.drawText(key, x, y, keyPaint)
        canvas.drawText(value, x + 160f, y, valPaint)
    }

    private fun createOutputFile(context: Context, filename: String): File {
        val dir = File(context.filesDir, "reports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, filename)
    }
}
