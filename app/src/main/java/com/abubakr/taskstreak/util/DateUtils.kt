package com.abubakr.taskstreak.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

object DateUtils {
    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.US)
    val MONTH_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    val SHORT_MONTH_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM", Locale.getDefault())

    fun today(): LocalDate = LocalDate.now()

    fun todayString(): String = today().format(DATE_FORMATTER)

    fun parse(dateStr: String): LocalDate = LocalDate.parse(dateStr, DATE_FORMATTER)

    fun format(date: LocalDate): String = date.format(DATE_FORMATTER)

    fun daysBetween(start: LocalDate, end: LocalDate): Long = ChronoUnit.DAYS.between(start, end)

    fun formatTime12Hour(timeStr: String?, isArabic: Boolean = false): String {
        if (timeStr.isNullOrBlank()) return if (isArabic) "معطل" else "Off"
        val parts = timeStr.trim().split(":")
        if (parts.size < 2) return timeStr
        val hour24 = parts[0].trim().toIntOrNull() ?: return timeStr
        val minute = parts[1].trim().toIntOrNull() ?: return timeStr
        val isAm = hour24 < 12
        val hour12 = when {
            hour24 == 0 -> 12
            hour24 > 12 -> hour24 - 12
            else -> hour24
        }
        val amPm = if (isArabic) {
            if (isAm) "ص" else "م"
        } else {
            if (isAm) "AM" else "PM"
        }
        return String.format(Locale.US, "%d:%02d %s", hour12, minute, amPm)
    }
}
