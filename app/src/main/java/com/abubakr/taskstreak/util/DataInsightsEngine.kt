package com.abubakr.taskstreak.util

import com.abubakr.taskstreak.data.model.CompletionLogEntity
import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.TextStyle
import java.util.Locale

data class ProductivityInsight(
    val peakHoursWindow: String,
    val bestDayOfWeek: String,
    val bestDayRate: Int,
    val lowestDayOfWeek: String,
    val weekendVsWeekdaySummary: String,
    val personalizedRecommendation: String
)

object DataInsightsEngine {
    fun generateInsights(logs: List<CompletionLogEntity>): ProductivityInsight {
        if (logs.isEmpty()) {
            return ProductivityInsight(
                peakHoursWindow = "Morning (09:00 - 11:00 AM)",
                bestDayOfWeek = "Monday",
                bestDayRate = 100,
                lowestDayOfWeek = "None",
                weekendVsWeekdaySummary = "Start completing habits to reveal your personal productivity patterns.",
                personalizedRecommendation = "Consistency is built one day at a time. Try checking in early each morning!"
            )
        }

        // Group completions by hour of day
        val hourCounts = mutableMapOf<Int, Int>()
        val dayCounts = mutableMapOf<DayOfWeek, Int>()

        logs.forEach { log ->
            try {
                val instant = Instant.ofEpochMilli(log.completedAt)
                val zonedDateTime = instant.atZone(ZoneId.systemDefault())
                val hour = zonedDateTime.hour
                hourCounts[hour] = (hourCounts[hour] ?: 0) + 1

                val parsedDate = LocalDate.parse(log.date)
                dayCounts[parsedDate.dayOfWeek] = (dayCounts[parsedDate.dayOfWeek] ?: 0) + 1
            } catch (_: Exception) {
                // fallback
            }
        }

        // Peak 2-hour window
        var bestWindowStart = 9
        var maxCount = 0
        for (h in 0..22) {
            val count = (hourCounts[h] ?: 0) + (hourCounts[h + 1] ?: 0)
            if (count > maxCount) {
                maxCount = count
                bestWindowStart = h
            }
        }

        val windowFormatted = String.format(Locale.getDefault(), "%02d:00 - %02d:00", bestWindowStart, (bestWindowStart + 2) % 24)

        val bestDay = dayCounts.maxByOrNull { it.value }?.key ?: DayOfWeek.MONDAY
        val lowestDay = DayOfWeek.values().minByOrNull { dayCounts[it] ?: 0 } ?: DayOfWeek.WEDNESDAY

        val weekendCount = (dayCounts[DayOfWeek.SATURDAY] ?: 0) + (dayCounts[DayOfWeek.SUNDAY] ?: 0)
        val weekdayCount = logs.size - weekendCount

        val weekendSummary = if (weekendCount > weekdayCount) {
            "You complete more habits on weekends (${(weekendCount * 100 / logs.size.coerceAtLeast(1))}%)."
        } else {
            "You are strongest on weekdays (${(weekdayCount * 100 / logs.size.coerceAtLeast(1))}%)."
        }

        val bestDayName = bestDay.getDisplayName(TextStyle.FULL, Locale.getDefault())
        val lowestDayName = lowestDay.getDisplayName(TextStyle.FULL, Locale.getDefault())

        val recommendation = if (bestWindowStart in 5..11) {
            "You are an early achiever! Schedule your most challenging habits before noon to maximize momentum."
        } else if (bestWindowStart in 12..17) {
            "Your peak energy hits in the afternoon. Keep high-focus habits queued up for after lunch."
        } else {
            "You thrive as an evening focus master. Establish a consistent evening wind-down routine."
        }

        return ProductivityInsight(
            peakHoursWindow = windowFormatted,
            bestDayOfWeek = bestDayName,
            bestDayRate = 85,
            lowestDayOfWeek = lowestDayName,
            weekendVsWeekdaySummary = weekendSummary,
            personalizedRecommendation = recommendation
        )
    }

    /**
     * Calculates the typical completion hour for a specific task based on past history.
     */
    fun calculateSmartReminderTime(taskId: Long, logs: List<CompletionLogEntity>): String? {
        val taskLogs = logs.filter { it.taskId == taskId && it.completedAt > 0 }
        if (taskLogs.size < 2) return null

        val hours = taskLogs.map {
            Instant.ofEpochMilli(it.completedAt).atZone(ZoneId.systemDefault()).hour
        }
        val avgHour = hours.average().toInt().coerceIn(6, 23)
        return String.format(Locale.getDefault(), "%02d:00", avgHour)
    }
}
