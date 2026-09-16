package com.abubakr.taskstreak.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.abubakr.taskstreak.MainActivity
import com.abubakr.taskstreak.R

object NotificationHelper {
    const val CHANNEL_ID = "task_streak_reminders"
    const val CHANNEL_NAME = "Habit & Task Reminders"

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
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
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
            if (taskId != -1L) {
                putExtra("selected_task_id", taskId)
            }
        }
        val openPendingIntent = PendingIntent.getActivity(
            context,
            notificationId * 10 + 1,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openPendingIntent)

        if (isMorningOverview || taskId == -1L) {
            // Actions for Morning overview: "View Today" and "Skip today"
            val viewTodayIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_VIEW_TODAY
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val viewPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 2,
                viewTodayIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_menu_view, "View Today", viewPendingIntent)

            val skipIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_SKIP_TODAY
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val skipPendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 3,
                skipIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Skip today", skipPendingIntent)
        } else {
            // Task Specific Quick Actions: "✓ Done", "Snooze 1h", "Open"
            val doneIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_MARK_DONE
                putExtra(NotificationActionReceiver.EXTRA_TASK_ID, taskId)
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
            }
            val donePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 4,
                doneIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.checkbox_on_background, "✓ Done", donePendingIntent)

            val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
                action = NotificationActionReceiver.ACTION_SNOOZE_1H
                putExtra(NotificationActionReceiver.EXTRA_TASK_ID, taskId)
                putExtra(NotificationActionReceiver.EXTRA_NOTIFICATION_ID, notificationId)
                putExtra(NotificationActionReceiver.EXTRA_TITLE, title)
                putExtra(NotificationActionReceiver.EXTRA_MESSAGE, message)
            }
            val snoozePendingIntent = PendingIntent.getBroadcast(
                context,
                notificationId * 10 + 5,
                snoozeIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            builder.addAction(android.R.drawable.ic_lock_idle_alarm, "Snooze 1h", snoozePendingIntent)

            builder.addAction(android.R.drawable.ic_menu_directions, "Open", openPendingIntent)
        }

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(notificationId, builder.build())
    }
}
