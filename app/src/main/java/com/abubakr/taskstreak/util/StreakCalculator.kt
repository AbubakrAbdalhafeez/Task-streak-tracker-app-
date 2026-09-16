package com.abubakr.taskstreak.util

import com.abubakr.taskstreak.data.model.TaskEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

data class TaskStreakStats(
    val currentStreak: Int,
    val bestStreak: Int,
    val completionRate: Float, // 0 to 100
    val totalCompletions: Int,
    val totalScheduledDays: Int,
    val completedThisWeek: Int,
    val completedThisMonth: Int,
    val isCompletedToday: Boolean,
    val isScheduledToday: Boolean,
    val isStreakAtRisk: Boolean
)

data class OverallStats(
    val totalActiveTasks: Int,
    val completedTodayCount: Int,
    val totalScheduledTodayCount: Int,
    val totalCompletionsAllTime: Int,
    val averageCompletionRate: Float,
    val bestOverallStreak: Int,
    val activeTasksWithStreakAtRisk: List<TaskEntity>
)

object StreakCalculator {

    fun calculateTaskStats(
        task: TaskEntity,
        completionDates: Set<String>, // "yyyy-MM-dd"
        today: LocalDate = LocalDate.now()
    ): TaskStreakStats {
        val startDate = try {
            DateUtils.parse(task.startDate)
        } catch (e: Exception) {
            today
        }
        val endDate = task.endDate?.let {
            try {
                DateUtils.parse(it)
            } catch (e: Exception) {
                null
            }
        }

        val todayStr = DateUtils.format(today)
        val isCompletedToday = completionDates.contains(todayStr)
        val isScheduledToday = task.isScheduledOn(today, startDate, endDate)

        // 1. Calculate Current Streak with Grace Period
        var currentStreak = 0
        var checkDate = today

        if (isScheduledToday) {
            if (isCompletedToday) {
                currentStreak++
                checkDate = today.minusDays(1)
            } else {
                // Today is scheduled but not completed yet: GRACE PERIOD!
                // Do not break the streak. Start checking from yesterday.
                checkDate = today.minusDays(1)
            }
        } else {
            checkDate = today.minusDays(1)
        }

        // Walk backwards
        while (!checkDate.isBefore(startDate)) {
            val isSched = task.isScheduledOn(checkDate, startDate, endDate)
            if (isSched) {
                val dateStr = DateUtils.format(checkDate)
                if (completionDates.contains(dateStr)) {
                    currentStreak++
                } else {
                    // Streak broke on this scheduled day
                    break
                }
            }
            checkDate = checkDate.minusDays(1)
        }

        // 2. Calculate Best Streak
        var bestStreak = 0
        var runningStreak = 0

        // Limit iteration from startDate up to today
        val effectiveEnd = if (endDate != null && endDate.isBefore(today)) endDate else today
        var forwardDate = startDate

        while (!forwardDate.isAfter(effectiveEnd)) {
            val isSched = task.isScheduledOn(forwardDate, startDate, endDate)
            if (isSched) {
                val dateStr = DateUtils.format(forwardDate)
                val isComp = completionDates.contains(dateStr)

                if (isComp) {
                    runningStreak++
                    if (runningStreak > bestStreak) {
                        bestStreak = runningStreak
                    }
                } else {
                    if (forwardDate == today && !isCompletedToday) {
                        // Today grace period: does not reset running streak yet
                        if (runningStreak > bestStreak) {
                            bestStreak = runningStreak
                        }
                    } else {
                        runningStreak = 0
                    }
                }
            }
            forwardDate = forwardDate.plusDays(1)
        }

        if (currentStreak > bestStreak) {
            bestStreak = currentStreak
        }

        // 3. Calculate Scheduled Days up to today (or endDate)
        var totalScheduledDays = 0
        var countDate = startDate
        while (!countDate.isAfter(effectiveEnd)) {
            if (task.isScheduledOn(countDate, startDate, endDate)) {
                totalScheduledDays++
            }
            countDate = countDate.plusDays(1)
        }

        val totalCompletions = completionDates.size
        val completionRate = if (totalScheduledDays > 0) {
            ((totalCompletions.toFloat() / totalScheduledDays.toFloat()) * 100f).coerceIn(0f, 100f)
        } else if (totalCompletions > 0) {
            100f
        } else {
            0f
        }

        // 4. Completed this week (Monday to Sunday)
        val mondayThisWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sundayThisWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        var completedThisWeek = 0
        for (dateStr in completionDates) {
            try {
                val d = DateUtils.parse(dateStr)
                if (!d.isBefore(mondayThisWeek) && !d.isAfter(sundayThisWeek)) {
                    completedThisWeek++
                }
            } catch (_: Exception) {}
        }

        // 5. Completed this month
        val currentYearMonth = YearMonth.from(today)
        var completedThisMonth = 0
        for (dateStr in completionDates) {
            try {
                val d = DateUtils.parse(dateStr)
                if (YearMonth.from(d) == currentYearMonth) {
                    completedThisMonth++
                }
            } catch (_: Exception) {}
        }

        val isStreakAtRisk = currentStreak >= 3 && isScheduledToday && !isCompletedToday

        return TaskStreakStats(
            currentStreak = currentStreak,
            bestStreak = bestStreak,
            completionRate = completionRate,
            totalCompletions = totalCompletions,
            totalScheduledDays = totalScheduledDays,
            completedThisWeek = completedThisWeek,
            completedThisMonth = completedThisMonth,
            isCompletedToday = isCompletedToday,
            isScheduledToday = isScheduledToday,
            isStreakAtRisk = isStreakAtRisk
        )
    }

    fun calculateOverallStats(
        tasks: List<TaskEntity>,
        taskStatsMap: Map<Long, TaskStreakStats>
    ): OverallStats {
        var completedTodayCount = 0
        var totalScheduledTodayCount = 0
        var totalCompletionsAllTime = 0
        var totalRateSum = 0f
        var bestOverallStreak = 0
        val atRiskTasks = mutableListOf<TaskEntity>()

        for (task in tasks) {
            val stats = taskStatsMap[task.id] ?: continue
            if (stats.isScheduledToday) {
                totalScheduledTodayCount++
                if (stats.isCompletedToday) {
                    completedTodayCount++
                }
            }
            totalCompletionsAllTime += stats.totalCompletions
            totalRateSum += stats.completionRate
            if (stats.bestStreak > bestOverallStreak) {
                bestOverallStreak = stats.bestStreak
            }
            if (stats.isStreakAtRisk) {
                atRiskTasks.add(task)
            }
        }

        val averageRate = if (tasks.isNotEmpty()) totalRateSum / tasks.size else 0f

        return OverallStats(
            totalActiveTasks = tasks.size,
            completedTodayCount = completedTodayCount,
            totalScheduledTodayCount = totalScheduledTodayCount,
            totalCompletionsAllTime = totalCompletionsAllTime,
            averageCompletionRate = averageRate,
            bestOverallStreak = bestOverallStreak,
            activeTasksWithStreakAtRisk = atRiskTasks
        )
    }
}
