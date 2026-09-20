package com.abubakr.taskstreak.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Percent
import android.content.Intent
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abubakr.taskstreak.R
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.ui.components.ContributionHeatmap
import com.abubakr.taskstreak.ui.components.MonthlyBarChart
import com.abubakr.taskstreak.ui.components.MonthlyCalendarView
import com.abubakr.taskstreak.ui.theme.InfoBlue
import com.abubakr.taskstreak.ui.theme.SuccessGreen
import com.abubakr.taskstreak.ui.viewmodel.StreakViewModel
import com.abubakr.taskstreak.util.DateUtils
import java.time.YearMonth

@Composable
fun CalendarAnalyticsScreen(
    viewModel: StreakViewModel,
    initialSelectedTask: TaskEntity? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val logs by viewModel.logs.collectAsStateWithLifecycle()
    val taskCompletionsMap by viewModel.taskCompletionsMap.collectAsStateWithLifecycle()
    val taskStatsMap by viewModel.taskStatsMap.collectAsStateWithLifecycle()
    val overallStats by viewModel.overallStats.collectAsStateWithLifecycle()

    var selectedTask by remember(initialSelectedTask, tasks) {
        mutableStateOf(initialSelectedTask ?: tasks.firstOrNull())
    }
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var showTaskPicker by remember { mutableStateOf(false) }

    val activeCompletions = remember(selectedTask, taskCompletionsMap, logs) {
        selectedTask?.let { taskCompletionsMap[it.id] } ?: logs.map { it.date }.toSet()
    }

    val selectedStats = selectedTask?.let { taskStatsMap[it.id] }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Title
        item {
            Column {
                Text(
                    text = stringResource(R.string.calendar_analytics_title),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.calendar_analytics_sub),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Task Selector Dropdown
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("task_selector_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showTaskPicker = true }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val defaultColor = MaterialTheme.colorScheme.primary
                            val color = selectedTask?.let {
                                try { Color(android.graphics.Color.parseColor(it.categoryColorHex)) }
                                catch (_: Exception) { defaultColor }
                            } ?: defaultColor

                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = selectedTask?.title ?: "Select a Task",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = selectedTask?.category ?: "Task",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select task",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showTaskPicker,
                        onDismissRequest = { showTaskPicker = false }
                    ) {
                        val defaultColor = MaterialTheme.colorScheme.primary
                        tasks.forEach { t ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val c = try {
                                            Color(android.graphics.Color.parseColor(t.categoryColorHex))
                                        } catch (_: Exception) { defaultColor }
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(c)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(t.title)
                                    }
                                },
                                onClick = {
                                    selectedTask = t
                                    showTaskPicker = false
                                }
                            )
                        }
                    }
                }
            }
        }

        // Summary Statistics Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Completion",
                    value = "${(selectedStats?.completionRate ?: overallStats.averageCompletionRate).toInt()}%",
                    sub = "Scheduled days",
                    icon = Icons.Default.Percent,
                    tint = SuccessGreen,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Completions",
                    value = "${selectedStats?.totalCompletions ?: overallStats.totalCompletionsAllTime}",
                    sub = "Total done",
                    icon = Icons.Default.CheckCircle,
                    tint = InfoBlue,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "This Week",
                    value = "${selectedStats?.completedThisWeek ?: 0}",
                    sub = "Days done",
                    icon = Icons.Default.DateRange,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Best Streak",
                    value = "${selectedStats?.bestStreak ?: overallStats.bestOverallStreak}d",
                    sub = "Longest chain",
                    icon = Icons.Default.LocalFireDepartment,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Monthly Interactive Calendar
        item {
            MonthlyCalendarView(
                task = selectedTask,
                completionDates = activeCompletions,
                currentMonth = currentMonth,
                onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                onDayClick = { date ->
                    selectedTask?.let { task ->
                        val dateStr = DateUtils.format(date)
                        viewModel.toggleTaskOnDate(task.id, dateStr)
                    }
                }
            )
        }

        // 12-Week Contribution Heatmap
        item {
            ContributionHeatmap(
                task = selectedTask,
                completionDates = activeCompletions
            )
        }

        // Monthly Bar Chart for the year
        item {
            val relevantLogs = remember(selectedTask, logs) {
                if (selectedTask == null) logs
                else logs.filter { it.taskId == selectedTask?.id }
            }
            MonthlyBarChart(logs = relevantLogs)
        }

        // Share Analytics Report Button
        item {
            OutlinedButton(
                onClick = {
                    val report = buildString {
                        append("📊 Habit Progress Report:\n")
                        append("Task: ${selectedTask?.title ?: "All Habits"}\n")
                        selectedStats?.let {
                            append("🔥 Current Streak: ${it.currentStreak} days\n")
                            append("🏆 Best Streak: ${it.bestStreak} days\n")
                            append("📈 Completion Rate: ${it.completionRate.toInt()}%\n")
                            append("✅ Total Completed: ${it.totalCompletions} times\n")
                        } ?: run {
                            append("🔥 Total Completions: ${overallStats.totalCompletionsAllTime}\n")
                            append("⚡ Active Habits: ${overallStats.totalActiveTasks}\n")
                        }
                        append("\nTracked with TaskStreak 🔥")
                    }
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, report)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Share Progress Report"))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.share_progress_report))
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    sub: String,
    icon: ImageVector,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = sub,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
