package com.abubakr.taskstreak.widget

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
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
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.abubakr.taskstreak.MainActivity
import com.abubakr.taskstreak.data.db.AppDatabase
import com.abubakr.taskstreak.data.model.CompletionLogEntity
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.util.DateUtils
import com.abubakr.taskstreak.util.StreakCalculator

data class WidgetTaskItem(
    val task: TaskEntity,
    val isCompleted: Boolean,
    val streak: Int
)

class TodayTasksWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val db = AppDatabase.getDatabase(context)
        val today = DateUtils.today()
        val todayStr = DateUtils.todayString()

        val allTasks = db.taskDao().getAllTasksDirect()
        val allLogs = db.completionLogDao().getAllLogsDirect()
        val logsByTask = allLogs.groupBy { it.taskId }

        val todayLogs = allLogs.filter { it.date == todayStr }.map { it.taskId }.toSet()

        val scheduledTasks = allTasks.filter { !it.isArchived && it.isScheduledOn(today) }
        val taskItems = scheduledTasks.map { task ->
            val dates = logsByTask[task.id]?.map { it.date }?.toSet() ?: emptySet()
            val stats = StreakCalculator.calculateTaskStats(task, dates, today)
            WidgetTaskItem(
                task = task,
                isCompleted = todayLogs.contains(task.id),
                streak = stats.currentStreak
            )
        }

        val completedCount = taskItems.count { it.isCompleted }
        val totalCount = taskItems.size

        provideContent {
            GlanceTheme {
                val bgProvider = ColorProvider(day = Color(0xFFF9F9FB), night = Color(0xFF1E1F24))
                val surfaceProvider = ColorProvider(day = Color(0xFFFFFFFF), night = Color(0xFF28292E))
                val primaryText = ColorProvider(day = Color(0xFF191C1E), night = Color(0xFFE2E2E6))
                val secondaryText = ColorProvider(day = Color(0xFF70777C), night = Color(0xFF8C9199))
                val flameOrange = ColorProvider(day = Color(0xFFFF5722), night = Color(0xFFFF7043))
                val successGreen = ColorProvider(day = Color(0xFF10B981), night = Color(0xFF34D399))

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(bgProvider)
                        .cornerRadius(16.dp)
                        .padding(12.dp)
                ) {
                    Column(modifier = GlanceModifier.fillMaxSize()) {
                        // Header
                        Row(
                            modifier = GlanceModifier.fillMaxWidth().clickable(actionStartActivity<MainActivity>()),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = GlanceModifier.defaultWeight()) {
                                Text(
                                    text = "Today's Tasks 🔥",
                                    style = TextStyle(
                                        color = primaryText,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = GlanceModifier.height(2.dp))
                                Text(
                                    text = "$completedCount of $totalCount done today",
                                    style = TextStyle(
                                        color = secondaryText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }

                            // Quick progress badge
                            Box(
                                modifier = GlanceModifier
                                    .background(ColorProvider(day = Color(0xFFFFE0D2), night = Color(0xFF422116)))
                                    .cornerRadius(8.dp)
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                val ratio = if (totalCount > 0) "${(completedCount * 100) / totalCount}%" else "0%"
                                Text(
                                    text = ratio,
                                    style = TextStyle(
                                        color = flameOrange,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                        }

                        Spacer(modifier = GlanceModifier.height(8.dp))

                        // Task List or Empty State
                        if (taskItems.isEmpty()) {
                            Box(
                                modifier = GlanceModifier
                                    .fillMaxSize()
                                    .clickable(actionStartActivity<MainActivity>()),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "No tasks scheduled for today ✨",
                                        style = TextStyle(color = primaryText, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = GlanceModifier.height(4.dp))
                                    Text(
                                        text = "Tap to open app and create habits",
                                        style = TextStyle(color = secondaryText, fontSize = 11.sp)
                                    )
                                }
                            }
                        } else {
                            LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                                items(taskItems) { item ->
                                    val checkBg = if (item.isCompleted) successGreen else ColorProvider(day = Color(0xFFE2E8F0), night = Color(0xFF3F444D))
                                    val checkMark = if (item.isCompleted) "✓" else "  "

                                    Row(
                                        modifier = GlanceModifier
                                            .fillMaxWidth()
                                            .padding(vertical = 3.dp)
                                            .background(surfaceProvider)
                                            .cornerRadius(10.dp)
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Tap checkbox -> Toggle completion
                                        Box(
                                            modifier = GlanceModifier
                                                .size(24.dp)
                                                .background(checkBg)
                                                .cornerRadius(6.dp)
                                                .clickable(
                                                    actionRunCallback<ToggleTaskActionCallback>(
                                                        actionParametersOf(ToggleTaskIdKey to item.task.id)
                                                    )
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = checkMark,
                                                style = TextStyle(
                                                    color = ColorProvider(day = Color.White, night = Color.White),
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            )
                                        }

                                        Spacer(modifier = GlanceModifier.width(8.dp))

                                        // Tap title -> Open App
                                        Column(
                                            modifier = GlanceModifier
                                                .defaultWeight()
                                                .clickable(actionStartActivity<MainActivity>())
                                        ) {
                                            Text(
                                                text = item.task.title,
                                                maxLines = 1,
                                                style = TextStyle(
                                                    color = if (item.isCompleted) secondaryText else primaryText,
                                                    fontSize = 13.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            )
                                        }

                                        // Streak badge
                                        if (item.streak > 0) {
                                            Spacer(modifier = GlanceModifier.width(6.dp))
                                            Text(
                                                text = "🔥 ${item.streak}d",
                                                style = TextStyle(
                                                    color = flameOrange,
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
            }
        }
    }

    companion object {
        val ToggleTaskIdKey = ActionParameters.Key<Long>("task_id")
    }
}

class ToggleTaskActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val taskId = parameters[TodayTasksWidget.ToggleTaskIdKey] ?: return
        val db = AppDatabase.getDatabase(context)
        val today = DateUtils.todayString()
        val existingLog = db.completionLogDao().getLog(taskId, today)
        if (existingLog != null) {
            db.completionLogDao().delete(taskId, today)
        } else {
            db.completionLogDao().insert(
                CompletionLogEntity(
                    taskId = taskId,
                    date = today,
                    completedAt = System.currentTimeMillis()
                )
            )
        }
        WidgetUpdater.updateAllWidgets(context)
    }
}

class TodayTasksWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayTasksWidget()
}
