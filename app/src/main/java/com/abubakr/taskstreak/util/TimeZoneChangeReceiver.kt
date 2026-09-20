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
        if (intent.action == Intent.ACTION_TIMEZONE_CHANGED) {
            val tzId = intent.getStringExtra("time-zone") ?: TimeZone.getDefault().id
            Log.d("TimeZoneReceiver", "Device Time Zone changed to: $tzId")

            val prefs = SettingsPreferences(context)
            val isTravelMode = prefs.travelModeEnabled.value

            if (isTravelMode) {
                Log.d("TimeZoneReceiver", "Travel Mode is Active: Adjusting notification alarms to local time: $tzId")
                NotificationHelper.rescheduleAllReminders(context)
            } else {
                Log.d("TimeZoneReceiver", "Standard mode: Rescheduling reminders at local time.")
                NotificationHelper.rescheduleAllReminders(context)
            }
        }
    }
}
