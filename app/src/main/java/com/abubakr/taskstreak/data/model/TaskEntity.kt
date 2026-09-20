package com.abubakr.taskstreak.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.abubakr.taskstreak.util.DateUtils
import java.time.DayOfWeek
import java.time.LocalDate

enum class RecurrenceType {
    DAILY,
    CUSTOM,
    ONCE
}

@Entity(
    tableName = "tasks",
    indices = [
        Index(value = ["isArchived"]),
        Index(value = ["category"]),
        Index(value = ["createdAt"])
    ]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = "General",
    val categoryColorHex: String = "#FF6B35",
    val recurrenceType: String = RecurrenceType.DAILY.name,
    val customDaysOfWeek: String = "1,2,3,4,5,6,7", // 1=Monday..7=Sunday
    val startDate: String = DateUtils.todayString(), // "yyyy-MM-dd"
    val endDate: String? = null, // "yyyy-MM-dd"
    val reminderTime: String? = null, // "09:00"
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false,
    val note: String? = null,
    val autoCompleteWithSubtasks: Boolean = true,
    val pomodoroCount: Int = 0,
    val isHabit: Boolean = true,
    val blockedByTaskId: Long? = null,
    val reminderDays: String? = null
) {
    fun getParsedDaysOfWeek(): Set<DayOfWeek> {
        if (recurrenceType != RecurrenceType.CUSTOM.name) {
            return DayOfWeek.values().toSet()
        }
        return customDaysOfWeek.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 1..7 }
            .map { DayOfWeek.of(it) }
            .toSet()
    }

    fun isScheduledOn(date: LocalDate, parsedStartDate: LocalDate, parsedEndDate: LocalDate?): Boolean {
        if (date.isBefore(parsedStartDate)) return false
        if (parsedEndDate != null && date.isAfter(parsedEndDate)) return false

        return when (recurrenceType) {
            RecurrenceType.ONCE.name -> date == parsedStartDate
            RecurrenceType.DAILY.name -> true
            RecurrenceType.CUSTOM.name -> {
                val days = getParsedDaysOfWeek()
                date.dayOfWeek in days
            }
            else -> true
        }
    }

    fun isScheduledOn(date: LocalDate): Boolean {
        val start = try { com.abubakr.taskstreak.util.DateUtils.parse(startDate) } catch (_: Exception) { date }
        val end = endDate?.let { try { com.abubakr.taskstreak.util.DateUtils.parse(it) } catch (_: Exception) { null } }
        return isScheduledOn(date, start, end)
    }
}
