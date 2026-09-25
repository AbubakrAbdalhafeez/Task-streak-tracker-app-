package com.abubakr.taskstreak.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.abubakr.taskstreak.MainActivity
import com.abubakr.taskstreak.R
import com.abubakr.taskstreak.data.db.AppDatabase
import com.abubakr.taskstreak.data.model.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

object NotificationHelper {
    const val CHANNEL_ID = "task_streak_reminders"
    const val CHANNEL_NAME = "Habit & Task Reminders"
    const val FOCUS_CHANNEL_ID = "task_streak_pomodoro"
    const val FOCUS_CHANNEL_NAME = "Focus & Pomodoro Alerts"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily morning alerts and task streak reminders"
                enableVibration(true)
            }

            val focusChannel = NotificationChannel(
                FOCUS_CHANNEL_ID,
                FOCUS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Session completion, break alerts, and 1-minute warnings"
                enableVibration(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
            manager.createNotificationChannel(focusChannel)
        }
    }

    fun showReminderNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = 1001,
        taskId: Long = -1L,
        isMorningOverview: Boolean = false
    ) {
        val localizedContext = LocalizationManager.getLocalizedContext(context)
        createNotificationChannel(localizedContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    localizedContext,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val openIntent = Intent(localizedContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (taskId != -1L) {
                putExtra("selected_task_id", taskId)
            }
        }
        val openPendingIntent = PendingIntent.getActivity(
            localizedContext,
            notificationId * 10 + 1,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(localizedContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)

        if (isMorningOverview || taskId == -1L) {
            // Actions for Morning overview: "View Today" and "Skip today"
            val viewTodayIntent = Intent(localizedContext, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_VIEW_TODAY
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val viewPendingIntent = PendingIntent.getBroadcast(
                localizedContext,
                notificationId * 10 + 2,
                viewTodayIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_menu_view, localizedContext.getString(R.string.notif_view_today), viewPendingIntent)

            val skipIntent = Intent(localizedContext, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_SKIP_TODAY
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val skipPendingIntent = PendingIntent.getBroadcast(
                localizedContext,
                notificationId * 10 + 3,
                skipIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, localizedContext.getString(R.string.notif_skip_today), skipPendingIntent)
        } else {
            // Task Specific Quick Actions: "✓ Done", "Snooze 1h", "Open"
            val doneIntent = Intent(localizedContext, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_MARK_DONE
                putExtra(NotificationActionReceiver.EXTRA_TASK_ID, taskId)
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val donePendingIntent = PendingIntent.getBroadcast(
                localizedContext,
                notificationId * 10 + 4,
                doneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.checkbox_on_background, localizedContext.getString(R.string.notif_done), donePendingIntent)

            val snoozeIntent = Intent(localizedContext, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_SNOOZE_1H
                putExtra(NotificationActionReceiver.EXTRA_TASK_ID, taskId)
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(NotificationActionReceiver.EXTRA_TITLE, title)
                putExtra(NotificationActionReceiver.EXTRA_MESSAGE, message)
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                localizedContext,
                notificationId * 10 + 5,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_lock_idle_alarm, localizedContext.getString(R.string.notif_snooze), snoozePendingIntent)

            builder.addAction(android.R.drawable.ic_menu_directions, localizedContext.getString(R.string.notif_open), openPendingIntent)
        }

        val manager = localizedContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }

    fun showFocusNotification(
        context: Context,
        title: String,
        message: String,
        notificationId: Int = 2001
    ) {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val openIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("nav_destination", "focus")
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            notificationId * 10 + 9,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, FOCUS_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }

    fun scheduleTaskReminder(context: Context, task: TaskEntity) {
        val reminderStr = task.reminderTime
        if (reminderStr.isNullOrBlank() || task.isArchived) {
            cancelTaskReminder(context, task.id)
            return
        }

        val parts = reminderStr.split(":")
        if (parts.size < 2) return
        val hour = parts[0].trim().toIntOrNull() ?: return
        val minute = parts[1].trim().toIntOrNull() ?: return

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        val localizedContext = LocalizationManager.getLocalizedContext(context)
        val intent = Intent(localizedContext, NotificationReceiver::class.java).apply {
            putExtra("task_id", task.id)
            putExtra("title", task.title)
            putExtra("message", localizedContext.getString(R.string.notif_time_for, task.title))
            putExtra("notification_id", task.id.toInt())
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("NotificationHelper", "Scheduled reminder for task ${task.id} (${task.title}) at ${calendar.time}")
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun cancelTaskReminder(context: Context, taskId: Long) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            alarmManager?.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("NotificationHelper", "Cancelled reminder for task $taskId")
        }
    }

    fun scheduleMorningOverviewReminder(context: Context, timeStr: String, enabled: Boolean) {
        val localizedContext = LocalizationManager.getLocalizedContext(context)
        val intent = Intent(localizedContext, NotificationReceiver::class.java).apply {
            putExtra("is_morning", true)
            putExtra("title", localizedContext.getString(R.string.notif_morning_title))
            putExtra("message", localizedContext.getString(R.string.notif_morning_msg))
            putExtra("notification_id", 1000)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        if (!enabled) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("NotificationHelper", "Cancelled morning overview reminder")
            return
        }

        val parts = timeStr.split(":")
        val hour = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 8
        val minute = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0

        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("NotificationHelper", "Scheduled morning overview at ${calendar.time}")
        } catch (e: SecurityException) {
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    fun rescheduleAllReminders(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val activeTasks = db.taskDao().getAllTasksDirect()
                activeTasks.filter { !it.isArchived && !it.reminderTime.isNullOrBlank() }.forEach { task ->
                    scheduleTaskReminder(context, task)
                }

                // Also schedule morning overview from preferences
                val prefs = com.abubakr.taskstreak.data.preferences.SettingsPreferences(context)
                scheduleMorningOverviewReminder(
                    context,
                    prefs.morningReminderTime.value,
                    prefs.morningReminderEnabled.value
                )

                Log.d("NotificationHelper", "All habit reminders successfully rescheduled (${activeTasks.size} tasks checked).")
            } catch (e: Exception) {
                Log.e("NotificationHelper", "Failed to reschedule reminders", e)
            }
        }
    }
}
