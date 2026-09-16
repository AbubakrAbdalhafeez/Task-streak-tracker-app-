package com.abubakr.taskstreak.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abubakr.taskstreak.data.model.RecurrenceType
import com.abubakr.taskstreak.data.model.SubtaskEntity
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.ui.theme.DangerRed
import com.abubakr.taskstreak.ui.theme.FlamePrimary
import com.abubakr.taskstreak.ui.theme.FlameSecondary
import com.abubakr.taskstreak.ui.theme.SuccessGreen
import com.abubakr.taskstreak.util.TaskStreakStats
import java.time.DayOfWeek

@Composable
fun TaskCard(
    task: TaskEntity,
    stats: TaskStreakStats?,
    subtasks: List<SubtaskEntity> = emptyList(),
    onToggleDoneToday: () -> Unit,
    onEditTask: () -> Unit,
    onDeleteTask: () -> Unit,
    onViewCalendar: () -> Unit,
    onToggleSubtask: ((SubtaskEntity, Boolean) -> Unit)? = null,
    onStartPomodoro: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var expandSubtasks by remember { mutableStateOf(false) }
    var showFullNote by remember { mutableStateOf(false) }
    val isDone = stats?.isCompletedToday == true
    val currentStreak = stats?.currentStreak ?: 0
    val bestStreak = stats?.bestStreak ?: 0
    val completionRate = stats?.completionRate ?: 0f

    val completedSubs = subtasks.count { it.isCompleted }
    val totalSubs = subtasks.size

    val categoryColor = remember(task.categoryColorHex) {
        try {
            Color(android.graphics.Color.parseColor(task.categoryColorHex))
        } catch (e: Exception) {
            FlamePrimary
        }
    }

    val recurrenceLabel = when (task.recurrenceType) {
        RecurrenceType.DAILY.name -> "Daily"
        RecurrenceType.ONCE.name -> "Once (${task.startDate})"
        RecurrenceType.CUSTOM.name -> {
            val days = task.getParsedDaysOfWeek()
            if (days.size == 7) "Daily"
            else {
                val dayNames = listOf(
                    DayOfWeek.SUNDAY to "Sun",
                    DayOfWeek.MONDAY to "Mon",
                    DayOfWeek.TUESDAY to "Tue",
                    DayOfWeek.WEDNESDAY to "Wed",
                    DayOfWeek.THURSDAY to "Thu",
                    DayOfWeek.FRIDAY to "Fri",
                    DayOfWeek.SATURDAY to "Sat"
                )
                dayNames.filter { it.first in days }.joinToString("·") { it.second }
            }
        }
        else -> "Daily"
    }

    val cardBg by animateColorAsState(
        targetValue = if (isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        else MaterialTheme.colorScheme.surface,
        animationSpec = spring(),
        label = "card_bg"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDone) 0.dp else 2.dp),
        border = if (isDone) null else androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Done Checkbox Button with strong tactile feel
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDone) SuccessGreen
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .border(
                            width = if (isDone) 0.dp else 2.dp,
                            color = if (isDone) Color.Transparent else categoryColor.copy(alpha = 0.6f),
                            shape = CircleShape
                        )
                        .clickable { onToggleDoneToday() }
                        .testTag("task_done_toggle_${task.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isDone) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Completed",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Title & recurrence details
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
                        ),
                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        else MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Category chip
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = categoryColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = task.category,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = categoryColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        // Recurrence info
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = recurrenceLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        // Reminder time if present
                        if (!task.reminderTime.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = FlameSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = task.reminderTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = FlameSecondary
                                )
                            }
                        }

                        // Pomodoro Count Badge
                        if (task.pomodoroCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = FlamePrimary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "🍅 x ${task.pomodoroCount}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = FlamePrimary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }

                        // Note Indicator Icon
                        if (!task.note.isNullOrBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "View Note",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { showFullNote = !showFullNote }
                            )
                        }
                    }

                    // Subtask Progress Indicator Chip
                    if (totalSubs > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                                .clickable { expandSubtasks = !expandSubtasks }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Subtasks: $completedSubs/$totalSubs",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = if (completedSubs == totalSubs) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(
                                imageVector = if (expandSubtasks) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Options menu button
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("task_options_menu_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Start Pomodoro / مؤقت بومودورو") },
                            onClick = {
                                showMenu = false
                                onStartPomodoro?.invoke()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = FlamePrimary)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("View Calendar / التقويم") },
                            onClick = {
                                showMenu = false
                                onViewCalendar()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Task / تعديل") },
                            onClick = {
                                showMenu = false
                                onEditTask()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete / حذف", color = DangerRed) },
                            onClick = {
                                showMenu = false
                                onDeleteTask()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed)
                            }
                        )
                    }
                }
            }

            // Expandable Note Card
            AnimatedVisibility(visible = showFullNote && !task.note.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "📝 " + (task.note ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Expandable Subtask List
            AnimatedVisibility(visible = expandSubtasks && subtasks.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    subtasks.forEach { sub ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f))
                                .clickable {
                                    onToggleSubtask?.invoke(sub, !sub.isCompleted)
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = if (sub.isCompleted) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                contentDescription = null,
                                tint = if (sub.isCompleted) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = sub.title,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    textDecoration = if (sub.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                                ),
                                color = if (sub.isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Streaks and statistics pill strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Current Streak
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Current streak",
                        tint = if (currentStreak > 0) FlamePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "$currentStreak day${if (currentStreak == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = if (currentStreak > 0) FlamePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Best Streak
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = "Best streak",
                        tint = FlameSecondary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "Best: $bestStreak",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Completion Rate
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${completionRate.toInt()}% rate",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = if (completionRate >= 80f) SuccessGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Quick Calendar Link
                IconButton(
                    onClick = onViewCalendar,
                    modifier = Modifier.size(24.dp).testTag("task_quick_calendar_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = "Calendar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
