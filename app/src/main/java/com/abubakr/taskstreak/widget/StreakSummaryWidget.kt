package com.abubakr.taskstreak.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.abubakr.taskstreak.MainActivity
import com.abubakr.taskstreak.data.db.AppDatabase
import com.abubakr.taskstreak.util.DateUtils
import com.abubakr.taskstreak.util.StreakCalculator

class StreakSummaryWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val today = DateUtils.today()
        val todayStr = DateUtils.todayString()

        val allTasks = db.taskDao().getAllTasksDirect()
        val allLogs = db.completionLogDao().getAllLogsDirect()
        val logsByTask = allLogs.groupBy { it.taskId }

        val activeTasks = allTasks.filter { !it.isArchived }
        val scheduledToday = activeTasks.filter { it.isScheduledOn(today) }
        val todayLogs = allLogs.filter { it.date == todayStr }.map { it.taskId }.toSet()

        val completedToday = scheduledToday.count { todayLogs.contains(it.id) }
        val totalToday = scheduledToday.size

        var maxCurrentStreak = 0
        var maxBestStreak = 0

        for (task in activeTasks) {
            val dates = logsByTask[task.id]?.map { it.date }?.toSet() ?: emptySet()
            val stats = StreakCalculator.calculateTaskStats(task, dates, today)
            if (stats.currentStreak > maxCurrentStreak) {
                maxCurrentStreak = stats.currentStreak
            }
            if (stats.bestStreak > maxBestStreak) {
                maxBestStreak = stats.bestStreak
            }
        }

        provideContent {
            GlanceTheme {
                val bgProvider = ColorProvider(day = Color(0xFFF9F9FB), night = Color(0xFF1E1F24))
                val cardProvider = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF28292E))
                val primaryText = ColorProvider(day = Color(0xFF191C1E), night = Color(0xFFE2E2E6))
                val secondaryText = ColorProvider(day = Color(0xFF70777C), night = Color(0xFF8C9199))
                val flameOrange = ColorProvider(day = Color(0xFFFF5722), night = Color(0xFFFF7043))
                val trophyGold = ColorProvider(day = Color(0xFFF59E0B), night = Color(0xFFFBBF24))

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(bgProvider)
                        .cornerRadius(16.dp)
                        .padding(10.dp)
                        .clickable(actionStartActivity<MainActivity>())
                ) {
                    Column(
                        modifier = GlanceModifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Title
                        Text(
                            text = "Streak Summary 🔥",
                            style = TextStyle(
                                color = primaryText,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        // 2x2 Stats Row
                        Row(
                            modifier = GlanceModifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Current Streak
                            Column(
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .background(cardProvider)
                                    .cornerRadius(10.dp)
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$maxCurrentStreak",
                                    style = TextStyle(
                                        color = flameOrange,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Current",
                                    style = TextStyle(
                                        color = secondaryText,
                                        fontSize = 10.sp
                                    )
                                )
                            }

                            Spacer(modifier = GlanceModifier.width(6.dp))

                            // Best Streak
                            Column(
                                modifier = GlanceModifier
                                    .defaultWeight()
                                    .background(cardProvider)
                                    .cornerRadius(10.dp)
                                    .padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$maxBestStreak",
                                    style = TextStyle(
                                        color = trophyGold,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = "Best 🏆",
                                    style = TextStyle(
                                        color = secondaryText,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        // Today's Progress Bottom Banner
                        Box(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .background(cardProvider)
                                .cornerRadius(8.dp)
                                .padding(vertical = 4.dp, horizontal = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            val ratioText = if (totalToday > 0) {
                                "Today: $completedToday of $totalToday done"
                            } else {
                                "No tasks today"
                            }
                            Text(
                                text = ratioText,
                                style = TextStyle(
                                    color = primaryText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

class StreakSummaryWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreakSummaryWidget()
}
