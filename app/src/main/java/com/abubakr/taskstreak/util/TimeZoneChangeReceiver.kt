package com.abubakr.taskstreak.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.abubakr.taskstreak.data.preferences.SettingsPreferences
import java.time.ZoneId
import java.util.TimeZone

class TimeZoneChangeReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_TIMEZONE_CHANGED || action == Intent.ACTION_TIME_CHANGED || action == Intent.ACTION_BOOT_COMPLETED) {
            val tzId = intent.getStringExtra("time-zone") ?: TimeZone.getDefault().id
            Log.d("TimeZoneReceiver", "Received $action. Time Zone: $tzId. Rescheduling reminders.")
            NotificationHelper.rescheduleAllReminders(context)
        }
    }
}
