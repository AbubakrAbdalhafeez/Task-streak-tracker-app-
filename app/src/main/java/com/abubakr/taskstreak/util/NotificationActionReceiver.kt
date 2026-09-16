package com.abubakr.taskstreak.util

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import com.abubakr.taskstreak.MainActivity
import com.abubakr.taskstreak.data.db.AppDatabase
import com.abubakr.taskstreak.data.model.CompletionLogEntity
import com.abubakr.taskstreak.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MARK_DONE = "com.abubakr.taskstreak.ACTION_MARK_DONE"
        const val ACTION_SNOOZE_1H = "com.abubakr.taskstreak.ACTION_SNOOZE_1H"
        const val ACTION_SKIP_TODAY = "com.abubakr.taskstreak.ACTION_SKIP_TODAY"
        const val ACTION_VIEW_TODAY = "com.abubakr.taskstreak.ACTION_VIEW_TODAY"

        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_NOTIFICATION_ID = "extra_notification_id"
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = intent.getIntExtra(EXTRA_NOTIFICATION_ID, 1001)

        when (intent.action) {
            ACTION_MARK_DONE -> {
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
                if (taskId != -1L) {
                    val pendingResult = goAsync()
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getDatabase(context)
                            val today = DateUtils.todayString()
                            db.completionLogDao().insert(
                                CompletionLogEntity(
                                    taskId = taskId,
                                    date = today,
                                    completedAt = System.currentTimeMillis()
                                )
                            )
                            WidgetUpdater.updateAllWidgets(context)
                            notificationManager.cancel(notificationId)
                        } finally {
                            pendingResult.finish()
                        }
                    }
                } else {
                    notificationManager.cancel(notificationId)
                }
            }

            ACTION_SNOOZE_1H -> {
                notificationManager.cancel(notificationId)
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Task Reminder"
                val message = intent.getStringExtra(EXTRA_MESSAGE) ?: "Time to complete your habit!"
                val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)

                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                val snoozeIntent = Intent(context, NotificationReceiver::class.java).apply {
                    putExtra("title", title)
                    putExtra("message", message)
                    putExtra("task_id", taskId)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    (System.currentTimeMillis() % 100000).toInt(),
                    snoozeIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val triggerTime = System.currentTimeMillis() + 60 * 60 * 1000L // 1 hour
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
                }
                Toast.makeText(context, "Reminder snoozed for 1 hour ⏰", Toast.LENGTH_SHORT).show()
            }

            ACTION_SKIP_TODAY -> {
                notificationManager.cancel(notificationId)
                Toast.makeText(context, "Task skipped for today", Toast.LENGTH_SHORT).show()
            }

            ACTION_VIEW_TODAY -> {
                notificationManager.cancel(notificationId)
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(mainIntent)
            }
        }
    }
}
