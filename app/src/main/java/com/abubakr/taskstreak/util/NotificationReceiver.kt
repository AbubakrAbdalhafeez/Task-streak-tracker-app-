package com.abubakr.taskstreak.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
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
    }
}
