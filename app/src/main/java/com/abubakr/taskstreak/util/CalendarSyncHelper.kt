package com.abubakr.taskstreak.util

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import com.abubakr.taskstreak.data.model.TaskEntity
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object CalendarSyncHelper {
    /**
     * Launches the system calendar app to add a task with reminder time and note.
     * Uses zero-permission CalendarContract ACTION_INSERT intent.
     */
    fun addTaskToCalendar(context: Context, task: TaskEntity) {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "🔥 ${task.title}")
            putExtra(CalendarContract.Events.DESCRIPTION, task.note ?: "Task Streak Tracker Habit")

            val today = LocalDate.now()
            val time = task.reminderTime?.let {
                try { LocalTime.parse(it) } catch (_: Exception) { LocalTime.of(9, 0) }
            } ?: LocalTime.of(9, 0)

            val startMillis = today.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = startMillis + (30 * 60 * 1000) // 30 minutes duration

            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)

            // Recurrence rule if recurring
            if (task.recurrenceType == "DAILY") {
                putExtra(CalendarContract.Events.RRULE, "FREQ=DAILY")
            } else if (task.recurrenceType == "CUSTOM") {
                putExtra(CalendarContract.Events.RRULE, "FREQ=WEEKLY")
            }
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback if no calendar app
        }
    }
}
