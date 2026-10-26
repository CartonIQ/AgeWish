package com.agewish.calculator.util

import java.text.NumberFormat
import java.util.Locale

object ResultFormatter {

    private val numberFormat = NumberFormat.getNumberInstance(Locale.getDefault())

    fun formatNumber(number: Long): String {
        return numberFormat.format(number)
    }

    fun formatAgeClipboard(result: AgeCalculationResult): String {
        val sb = StringBuilder()
        sb.append("AgeWish – Age Calculation Report\n")
        sb.append("---------------------------------\n")
        sb.append("Date of Birth: ${result.dob.format(DateUtils.DATE_FORMATTER)}\n")
        sb.append("Calculated as of: ${result.asOf.format(DateUtils.DATE_FORMATTER)}\n")
        sb.append("Age: ${result.years} years, ${result.months} months, ${result.days} days\n")
        sb.append("Total Days: ${formatNumber(result.totalDays)}\n")
        sb.append("Total Weeks: ${formatNumber(result.totalWeeks)}\n")
        sb.append("Total Months: ${formatNumber(result.totalMonths)}\n")
        sb.append("Total Hours: ${formatNumber(result.totalHours)}\n")
        sb.append("Total Minutes: ${formatNumber(result.totalMinutes)}\n")
        if (result.isBirthdayToday) {
            sb.append("Birthday: Today is your Birthday! Happy Birthday! 🎂🎉\n")
        } else {
            sb.append("Next Birthday: in ${result.daysToNextBirthday} days (${result.nextBirthdayDate.format(DateUtils.DATE_FORMATTER)})\n")
        }
        result.anniversaryResult?.let { anniv ->
            if (anniv.isAnniversaryToday) {
                sb.append("Anniversary: Today is your Anniversary! Happy Anniversary! 💍❤️\n")
            } else {
                sb.append("Next Anniversary: in ${anniv.daysToNextAnniversary} days (${anniv.nextAnniversaryDate.format(DateUtils.DATE_FORMATTER)})\n")
            }
        }
        sb.append("Zodiac Sign: ${result.westernZodiac}\n")
        sb.append("Chinese Zodiac: ${result.chineseZodiac}\n")
        sb.append("Born on: ${result.dayOfWeekBorn}\n")
        sb.append("---------------------------------\n")
        sb.append("Generated with AgeWish – Calculate age. Send wishes.")
        return sb.toString()
    }

    fun formatDateDiffClipboard(result: DateDifferenceResult): String {
        val sb = StringBuilder()
        sb.append("AgeWish – Date Difference Report\n")
        sb.append("---------------------------------\n")
        sb.append("Start Date: ${result.startDate.format(DateUtils.DATE_FORMATTER)}\n")
        sb.append("End Date: ${result.endDate.format(DateUtils.DATE_FORMATTER)}\n")
        sb.append("Include End Day: ${if (result.includeEndDay) "Yes (+1 day)" else "No"}\n")
        sb.append("Difference: ${result.years} years, ${result.months} months, ${result.days} days\n")
        sb.append("Total Days: ${formatNumber(result.totalDays)} (Weeks: ${formatNumber(result.totalWeeks)})\n")
        result.workingDays?.let {
            sb.append("Working Days: ${formatNumber(it)} (excluding Sat/Sun)\n")
        }
        sb.append("---------------------------------\n")
        sb.append("Generated with AgeWish – Calculate age. Send wishes.")
        return sb.toString()
    }

    fun formatDatePlusClipboard(result: DatePlusResult): String {
        val sb = StringBuilder()
        sb.append("AgeWish – Date Math Report\n")
        sb.append("---------------------------------\n")
        sb.append("Base Date: ${result.baseDate.format(DateUtils.DATE_FORMATTER)}\n")
        val unit = if (result.isMonthsMode) "month(s)" else "day(s)"
        sb.append("Offset: ${if (result.amount >= 0) "+${result.amount}" else "${result.amount}"} $unit\n")
        sb.append("Result Date: ${result.resultDate.format(DateUtils.DATE_FORMATTER)} (${result.dayOfWeek})\n")
        sb.append("---------------------------------\n")
        sb.append("Generated with AgeWish – Calculate age. Send wishes.")
        return sb.toString()
    }
}
