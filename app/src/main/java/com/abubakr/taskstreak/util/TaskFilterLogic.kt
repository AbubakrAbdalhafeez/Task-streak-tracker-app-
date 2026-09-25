package com.abubakr.taskstreak.util

import com.abubakr.taskstreak.data.model.RecurrenceType
import com.abubakr.taskstreak.data.model.TaskEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

enum class TimeframeFilter(val titleEn: String, val titleAr: String) {
    OVERDUE("Overdue", "متأخرة"),
    TODAY("Today", "اليوم"),
    TOMORROW("Tomorrow", "غداً"),
    THIS_WEEK("This Week", "هذا الأسبوع"),
    THIS_MONTH("This Month", "هذا الشهر");

    fun getDisplayName(isArabic: Boolean): String = if (isArabic) titleAr else titleEn
}

enum class TaskInternalFilter(val titleEn: String, val titleAr: String) {
    ALL_STATUS("All Status", "كل الحالات"),
    HABITS("Habits", "العادات"),
    PENDING("Pending", "قيد الانتظار"),
    COMPLETED("Completed", "المكتملة"),
    ONE_TIME("One-Time Tasks", "مهام لمرة واحدة");

    fun getDisplayName(isArabic: Boolean): String = if (isArabic) titleAr else titleEn
}

object TaskFilterLogic {

    fun isTaskInTimeframe(
        task: TaskEntity,
        timeframe: TimeframeFilter,
        taskCompletions: Set<String>,
        today: LocalDate = LocalDate.now()
    ): Boolean {
        val taskStart = try { DateUtils.parse(task.startDate) } catch (_: Exception) { today }
        val taskEnd = task.endDate?.let { try { DateUtils.parse(it) } catch (_: Exception) { null } }

        return when (timeframe) {
            TimeframeFilter.OVERDUE -> {
                // 1. One-time tasks: due date is in past AND not completed
                if (!task.isHabit || task.recurrenceType == RecurrenceType.ONCE.name) {
                    taskStart.isBefore(today) && !taskCompletions.contains(task.startDate)
                } else {
                    // 2. Habits: if past scheduled day was missed without completion
                    if (task.recurrenceType == RecurrenceType.DAILY.name) {
                        val yesterday = today.minusDays(1)
                        yesterday >= taskStart && (taskEnd == null || yesterday <= taskEnd) && !taskCompletions.contains(yesterday.toString())
                    } else {
                        // Custom habit: check most recent scheduled day in the past 7 days
                        val daysOfWeek = task.getParsedDaysOfWeek()
                        var isOverdue = false
                        for (daysBack in 1..7) {
                            val pastDate = today.minusDays(daysBack.toLong())
                            if (pastDate < taskStart) break
                            if (taskEnd != null && pastDate > taskEnd) continue
                            if (pastDate.dayOfWeek in daysOfWeek) {
                                if (!taskCompletions.contains(pastDate.toString())) {
                                    isOverdue = true
                                }
                                break
                            }
                        }
                        isOverdue
                    }
                }
            }

            TimeframeFilter.TODAY -> {
                task.isScheduledOn(today, taskStart, taskEnd)
            }

            TimeframeFilter.TOMORROW -> {
                val tomorrow = today.plusDays(1)
                task.isScheduledOn(tomorrow, taskStart, taskEnd)
            }

            TimeframeFilter.THIS_WEEK -> {
                val startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                val endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))

                if (!task.isHabit || task.recurrenceType == RecurrenceType.ONCE.name) {
                    !taskStart.isBefore(startOfWeek) && !taskStart.isAfter(endOfWeek)
                } else if (task.recurrenceType == RecurrenceType.DAILY.name) {
                    !taskStart.isAfter(endOfWeek) && (taskEnd == null || !taskEnd.isBefore(startOfWeek))
                } else {
                    val activeInWeek = !taskStart.isAfter(endOfWeek) && (taskEnd == null || !taskEnd.isBefore(startOfWeek))
                    activeInWeek && task.getParsedDaysOfWeek().isNotEmpty()
                }
            }

            TimeframeFilter.THIS_MONTH -> {
                val firstDayOfMonth = today.withDayOfMonth(1)
                val lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth())

                if (!task.isHabit || task.recurrenceType == RecurrenceType.ONCE.name) {
                    !taskStart.isBefore(firstDayOfMonth) && !taskStart.isAfter(lastDayOfMonth)
                } else if (task.recurrenceType == RecurrenceType.DAILY.name) {
                    !taskStart.isAfter(lastDayOfMonth) && (taskEnd == null || !taskEnd.isBefore(firstDayOfMonth))
                } else {
                    val activeInMonth = !taskStart.isAfter(lastDayOfMonth) && (taskEnd == null || !taskEnd.isBefore(firstDayOfMonth))
                    activeInMonth && task.getParsedDaysOfWeek().isNotEmpty()
                }
            }
        }
    }

    fun isTaskCompletedForTimeframe(
        task: TaskEntity,
        timeframe: TimeframeFilter,
        taskCompletions: Set<String>,
        isCompletedToday: Boolean,
        today: LocalDate = LocalDate.now()
    ): Boolean {
        return when (timeframe) {
            TimeframeFilter.TODAY -> isCompletedToday
            TimeframeFilter.TOMORROW -> taskCompletions.contains(today.plusDays(1).toString())
            TimeframeFilter.OVERDUE -> false
            TimeframeFilter.THIS_WEEK -> {
                if (!task.isHabit || task.recurrenceType == RecurrenceType.ONCE.name) {
                    taskCompletions.contains(task.startDate)
                } else {
                    isCompletedToday || taskCompletions.contains(today.toString())
                }
            }
            TimeframeFilter.THIS_MONTH -> {
                if (!task.isHabit || task.recurrenceType == RecurrenceType.ONCE.name) {
                    taskCompletions.contains(task.startDate)
                } else {
                    isCompletedToday || taskCompletions.contains(today.toString())
                }
            }
        }
    }

    fun matchesInternalFilter(
        task: TaskEntity,
        internalFilter: TaskInternalFilter,
        isCompleted: Boolean
    ): Boolean {
        return when (internalFilter) {
            TaskInternalFilter.ALL_STATUS -> true
            TaskInternalFilter.HABITS -> task.isHabit && task.recurrenceType != RecurrenceType.ONCE.name
            TaskInternalFilter.PENDING -> !isCompleted
            TaskInternalFilter.COMPLETED -> isCompleted
            TaskInternalFilter.ONE_TIME -> !task.isHabit || task.recurrenceType == RecurrenceType.ONCE.name
        }
    }
}
