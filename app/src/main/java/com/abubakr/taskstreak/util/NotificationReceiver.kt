package com.abubakr.taskstreak.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = com.abubakr.taskstreak.data.preferences.SettingsPreferences(context)
        if (prefs.isQuietHoursNow()) {
            // Suppress notifications during quiet hours
            return
        }

        val title = intent.getStringExtra("title") ?: "Streak Tracker"
        val message = intent.getStringExtra("message") ?: "Don't break your streak today! 🔥"
        val taskId = intent.getLongExtra("task_id", -1L)
        val notificationId = intent.getIntExtra("notification_id", if (taskId != -1L) taskId.toInt() else 1001)
        val isMorning = intent.getBooleanExtra("is_morning", false)

        NotificationHelper.showReminderNotification(
            context = context,
            title = title,
            message = message,
            notificationId = notificationId,
            taskId = taskId,
            isMorningOverview = isMorning
        )

        // Reschedule recurring task reminder for the next day
        if (taskId != -1L) {
            val pendingResult = goAsync()
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    val db = com.abubakr.taskstreak.data.db.AppDatabase.getDatabase(context)
                    val task = db.taskDao().getTaskByIdDirect(taskId)
                    if (task != null && !task.isArchived && !task.reminderTime.isNullOrBlank()) {
                        NotificationHelper.scheduleTaskReminder(context, task)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("NotificationReceiver", "Error re-arming task reminder", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
