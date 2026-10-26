package com.agewish.calculator.pdf

import android.content.Context
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.agewish.calculator.util.ThemePreset
import java.io.File
import java.io.FileOutputStream

object WishCardGenerator {

    private const val PAGE_WIDTH = 595 // A4 standard points
    private const val PAGE_HEIGHT = 842 // A4 standard points
    private const val PADDING = 45f

    fun generateWishCardPdf(
        context: Context,
        occasion: String,
        emoji: String,
        recipientName: String,
        senderName: String?,
        message: String,
        theme: ThemePreset
    ): File {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        // 1. Draw Full-page Elegant Gradient Background
        val gradientShader = LinearGradient(
            0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(),
            theme.startColor, theme.endColor,
            Shader.TileMode.CLAMP
        )
        val bgPaint = Paint().apply {
            shader = gradientShader
        }
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), bgPaint)

        // 2. Decorative inner border
        val borderPaint = Paint().apply {
            color = theme.accentColor
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        val borderMargin = 25f
        canvas.drawRoundRect(
            RectF(borderMargin, borderMargin, PAGE_WIDTH - borderMargin, PAGE_HEIGHT - borderMargin),
            20f, 20f,
            borderPaint
        )

        // Inner translucent card panel
        val panelPaint = Paint().apply {
            color = Color.argb(40, 255, 255, 255)
            style = Paint.Style.FILL
            isAntiAlias = true
        }
        val panelMargin = 38f
        canvas.drawRoundRect(
            RectF(panelMargin, panelMargin, PAGE_WIDTH - panelMargin, PAGE_HEIGHT - panelMargin),
            16f, 16f,
            panelPaint
        )

        // 3. Draw Occasion Icon / Emoji
        val emojiPaint = Paint().apply {
            textSize = 64f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        var currentY = 160f
        canvas.drawText(emoji, PAGE_WIDTH / 2f, currentY, emojiPaint)
        currentY += 55f

        // 4. Occasion Sub-tag (e.g. "CELEBRATING WITH YOU")
        val tagPaint = Paint().apply {
            color = theme.accentColor
            textSize = 12f
            letterSpacing = 0.2f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(occasion.uppercase(), PAGE_WIDTH / 2f, currentY, tagPaint)
        currentY += 45f

        // 5. Greeting Heading ("Happy Birthday, Rahul!")
        val headingTitle = if (occasion.equals("Birthday", ignoreCase = true)) {
            "Happy Birthday, $recipientName!"
        } else if (occasion.equals("Anniversary", ignoreCase = true)) {
            "Happy Anniversary, $recipientName!"
        } else if (occasion.equals("New Year", ignoreCase = true)) {
            "Happy New Year, $recipientName!"
        } else {
            "For Dear $recipientName"
        }

        val headingPaint = Paint().apply {
            color = theme.textColor
            textSize = 28f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(headingTitle, PAGE_WIDTH / 2f, currentY, headingPaint)
        currentY += 25f

        // Gold divider line
        val dividerPaint = Paint().apply {
            color = theme.accentColor
            strokeWidth = 2.5f
            isAntiAlias = true
        }
        val dividerHalfWidth = 60f
        canvas.drawLine(
            (PAGE_WIDTH / 2f) - dividerHalfWidth,
            currentY,
            (PAGE_WIDTH / 2f) + dividerHalfWidth,
            currentY,
            dividerPaint
        )
        currentY += 45f

        // 6. Centered Body Message
        val messagePaint = Paint().apply {
            color = theme.textColor
            textSize = 15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val maxTextWidth = PAGE_WIDTH - (PADDING * 2) - 40f
        val wrappedLines = wrapText(message, messagePaint, maxTextWidth)
        val lineHeight = 26f
        for (line in wrappedLines) {
            canvas.drawText(line, PAGE_WIDTH / 2f, currentY, messagePaint)
            currentY += lineHeight
        }

        // 7. Sender footer
        if (!senderName.isNullOrBlank()) {
            currentY += 35f
            val senderPaint = Paint().apply {
                color = theme.accentColor
                textSize = 17f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText("— With love & best wishes, $senderName", PAGE_WIDTH / 2f, currentY, senderPaint)
        }

        // 8. Branding footer at bottom
        val brandingPaint = Paint().apply {
            color = Color.argb(180, 255, 255, 255)
            textSize = 10f
            letterSpacing = 0.1f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("Made with AgeWish", PAGE_WIDTH / 2f, PAGE_HEIGHT - 45f, brandingPaint)

        document.finishPage(page)

        val outputFile = createOutputFile(context, "WishCard_${System.currentTimeMillis()}.pdf")
        FileOutputStream(outputFile).use { out ->
            document.writeTo(out)
        }
        document.close()
        return outputFile
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split("\\s+".toRegex())
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
            val width = paint.measureText(testLine)
            if (width > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            } else {
                currentLine = StringBuilder(testLine)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }

    private fun createOutputFile(context: Context, filename: String): File {
        val dir = File(context.filesDir, "reports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, filename)
    }
}
