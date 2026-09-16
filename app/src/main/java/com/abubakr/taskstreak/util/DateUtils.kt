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
}
