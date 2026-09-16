package com.abubakr.taskstreak.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object WidgetUpdater {
    fun updateAllWidgets(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                TodayTasksWidget().updateAll(context)
                StreakSummaryWidget().updateAll(context)
            } catch (_: Exception) {
            }
        }
    }
}
