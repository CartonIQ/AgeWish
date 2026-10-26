package com.agewish.calculator.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

data class AgeCalculationResult(
    val dob: LocalDate,
    val asOf: LocalDate,
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDays: Long,
    val totalWeeks: Long,
    val totalMonths: Long,
    val totalHours: Long,
    val totalMinutes: Long,
    val daysToNextBirthday: Long,
    val nextBirthdayDate: LocalDate,
    val isBirthdayToday: Boolean,
    val isBirthdayWithin7Days: Boolean,
    val anniversaryResult: AnniversaryResult?,
    val westernZodiac: String,
    val chineseZodiac: String,
    val dayOfWeekBorn: String
)

data class AnniversaryResult(
    val anniversaryDate: LocalDate,
    val daysToNextAnniversary: Long,
    val nextAnniversaryDate: LocalDate,
    val isAnniversaryToday: Boolean
)

data class DateDifferenceResult(
    val startDate: LocalDate,
    val endDate: LocalDate,
    val includeEndDay: Boolean,
    val years: Int,
    val months: Int,
    val days: Int,
    val totalDays: Long,
    val totalWeeks: Long,
    val workingDays: Long?
)

data class DatePlusResult(
    val baseDate: LocalDate,
    val amount: Long,
    val isMonthsMode: Boolean,
    val resultDate: LocalDate,
    val dayOfWeek: String
)

object DateUtils {

    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    val DISPLAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.getDefault())

    fun calculateAge(
        dob: LocalDate,
        asOf: LocalDate = LocalDate.now(),
        anniversary: LocalDate? = null
    ): AgeCalculationResult {
        require(!dob.isAfter(asOf)) { "Date of birth cannot be after the calculation date." }

        val period = Period.between(dob, asOf)
        val totalDays = ChronoUnit.DAYS.between(dob, asOf)
        val totalWeeks = totalDays / 7
        val totalMonths = ChronoUnit.MONTHS.between(dob, asOf)
        val totalHours = totalDays * 24L
        val totalMinutes = totalHours * 60L

        // Next birthday calculation
        val currentYearBday = adjustLeapDay(dob, asOf.year)
        val (nextBdayDate, daysToNextBday, isBdayToday) = when {
            dob.month == asOf.month && dob.dayOfMonth == asOf.dayOfMonth -> {
                Triple(asOf, 0L, true)
            }
            currentYearBday.isAfter(asOf) -> {
                val diff = ChronoUnit.DAYS.between(asOf, currentYearBday)
                Triple(currentYearBday, diff, false)
            }
            else -> {
                val nextYearBday = adjustLeapDay(dob, asOf.year + 1)
                val diff = ChronoUnit.DAYS.between(asOf, nextYearBday)
                Triple(nextYearBday, diff, false)
            }
        }
        val isBirthdayWithin7Days = daysToNextBday in 1..7

        // Optional Anniversary Calculation
        val anniversaryResult = anniversary?.let { annivDate ->
            val currentYearAnniv = adjustLeapDay(annivDate, asOf.year)
            val (nextAnnivDate, daysToNextAnniv, isAnnivToday) = when {
                annivDate.month == asOf.month && annivDate.dayOfMonth == asOf.dayOfMonth -> {
                    Triple(asOf, 0L, true)
                }
                currentYearAnniv.isAfter(asOf) -> {
                    val diff = ChronoUnit.DAYS.between(asOf, currentYearAnniv)
                    Triple(currentYearAnniv, diff, false)
                }
                else -> {
                    val nextYearAnniv = adjustLeapDay(annivDate, asOf.year + 1)
                    val diff = ChronoUnit.DAYS.between(asOf, nextYearAnniv)
                    Triple(nextYearAnniv, diff, false)
                }
            }
            AnniversaryResult(
                anniversaryDate = annivDate,
                daysToNextAnniversary = daysToNextAnniv,
                nextAnniversaryDate = nextAnnivDate,
                isAnniversaryToday = isAnnivToday
            )
        }

        val westernZodiac = ZodiacUtils.getWesternZodiac(dob.monthValue, dob.dayOfMonth)
        val chineseZodiac = ZodiacUtils.getChineseZodiac(dob.year)
        val dayOfWeekBorn = dob.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())

        return AgeCalculationResult(
            dob = dob,
            asOf = asOf,
            years = period.years,
            months = period.months,
            days = period.days,
            totalDays = totalDays,
            totalWeeks = totalWeeks,
            totalMonths = totalMonths,
            totalHours = totalHours,
            totalMinutes = totalMinutes,
            daysToNextBirthday = daysToNextBday,
            nextBirthdayDate = nextBdayDate,
            isBirthdayToday = isBdayToday,
            isBirthdayWithin7Days = isBirthdayWithin7Days,
            anniversaryResult = anniversaryResult,
            westernZodiac = westernZodiac,
            chineseZodiac = chineseZodiac,
            dayOfWeekBorn = dayOfWeekBorn
        )
    }

    fun calculateDateDifference(
        start: LocalDate,
        end: LocalDate,
        includeEndDay: Boolean = false,
        countWorkingDays: Boolean = false
    ): DateDifferenceResult {
        require(!start.isAfter(end)) { "Start date must be before or equal to end date." }

        val effectiveEnd = if (includeEndDay) end.plusDays(1) else end
        val period = Period.between(start, effectiveEnd)
        val totalDays = ChronoUnit.DAYS.between(start, effectiveEnd)
        val totalWeeks = totalDays / 7

        var workingDaysCount: Long? = null
        if (countWorkingDays) {
            var count = 0L
            var curr = start
            val limit = if (includeEndDay) end.plusDays(1) else end
            while (curr.isBefore(limit)) {
                if (curr.dayOfWeek != DayOfWeek.SATURDAY && curr.dayOfWeek != DayOfWeek.SUNDAY) {
                    count++
                }
                curr = curr.plusDays(1)
            }
            workingDaysCount = count
        }

        return DateDifferenceResult(
            startDate = start,
            endDate = end,
            includeEndDay = includeEndDay,
            years = period.years,
            months = period.months,
            days = period.days,
            totalDays = totalDays,
            totalWeeks = totalWeeks,
            workingDays = workingDaysCount
        )
    }

    fun calculateDatePlus(
        baseDate: LocalDate,
        amount: Long,
        isMonthsMode: Boolean = false
    ): DatePlusResult {
        val resultDate = if (isMonthsMode) {
            baseDate.plusMonths(amount)
        } else {
            baseDate.plusDays(amount)
        }
        val dayOfWeek = resultDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())

        return DatePlusResult(
            baseDate = baseDate,
            amount = amount,
            isMonthsMode = isMonthsMode,
            resultDate = resultDate,
            dayOfWeek = dayOfWeek
        )
    }

    private fun adjustLeapDay(date: LocalDate, targetYear: BooleanYearTarget): LocalDate {
        return try {
            LocalDate.of(targetYear, date.month, date.dayOfMonth)
        } catch (_: Exception) {
            // Feb 29 in a non-leap year falls back to Feb 28
            LocalDate.of(targetYear, date.month, 28)
        }
    }
}

typealias BooleanYearTarget = Int
