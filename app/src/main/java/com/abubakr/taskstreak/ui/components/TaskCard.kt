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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.abubakr.taskstreak.R
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
import com.abubakr.taskstreak.ui.theme.SuccessGreen
import com.abubakr.taskstreak.util.DateUtils
import com.abubakr.taskstreak.util.TaskStreakStats
import java.time.DayOfWeek

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonUnchecked

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TaskCard(
    task: TaskEntity,
    stats: TaskStreakStats?,
    subtasks: List<SubtaskEntity> = emptyList(),
    isBlocked: Boolean = false,
    blockerTitle: String? = null,
    onBlockedClick: (() -> Unit)? = null,
    isSelectionMode: Boolean = false,
    isSelected: Boolean = false,
    onToggleSelect: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    onToggleDoneToday: () -> Unit,
    onEditTask: () -> Unit,
    onDeleteTask: () -> Unit,
    onViewCalendar: () -> Unit,
    onChangeSchedule: (() -> Unit)? = null,
    onSetReminder: (() -> Unit)? = null,
    onDuplicateTask: (() -> Unit)? = null,
    onToggleSubtask: ((SubtaskEntity, Boolean) -> Unit)? = null,
    onStartPomodoro: (() -> Unit)? = null,
    onArchiveTask: (() -> Unit)? = null,
    onSyncCalendar: (() -> Unit)? = null,
    onShareStreak: (() -> Unit)? = null,
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

    val defaultPrimaryColor = MaterialTheme.colorScheme.primary
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"

    val categoryColor = remember(task.categoryColorHex) {
        try {
            Color(android.graphics.Color.parseColor(task.categoryColorHex))
        } catch (e: Exception) {
            defaultPrimaryColor
        }
    }

    val recurrenceLabel = when (task.recurrenceType) {
        RecurrenceType.DAILY.name -> if (isArabic) "يومياً" else "Daily"
        RecurrenceType.ONCE.name -> if (isArabic) "مرة واحدة (${task.startDate})" else "Once (${task.startDate})"
        RecurrenceType.CUSTOM.name -> {
            val days = task.getParsedDaysOfWeek()
            if (days.size == 7) if (isArabic) "يومياً" else "Daily"
            else {
                val dayNames = if (isArabic) listOf(
                    DayOfWeek.SUNDAY to "أحد",
                    DayOfWeek.MONDAY to "إثنين",
                    DayOfWeek.TUESDAY to "ثلاثاء",
                    DayOfWeek.WEDNESDAY to "أربعاء",
                    DayOfWeek.THURSDAY to "خميس",
                    DayOfWeek.FRIDAY to "جمعة",
                    DayOfWeek.SATURDAY to "سبت"
                ) else listOf(
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
        else -> if (isArabic) "يومياً" else "Daily"
    }

    val cardBg by animateColorAsState(
        targetValue = if (isDone) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        else MaterialTheme.colorScheme.surface,
        animationSpec = spring(),
        label = "card_bg"
    )

    val selectionBorder = if (isSelected) androidx.compose.foundation.BorderStroke(
        2.dp,
        defaultPrimaryColor
    ) else if (isDone) null else androidx.compose.foundation.BorderStroke(
        1.dp,
        MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) onToggleSelect?.invoke()
                },
                onLongClick = {
                    onLongClick?.invoke()
                }
            )
            .testTag("task_card_${task.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) defaultPrimaryColor.copy(alpha = 0.12f) else cardBg
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDone) 0.dp else 2.dp),
        border = selectionBorder
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
                if (isSelectionMode) {
                    IconButton(
                        onClick = { onToggleSelect?.invoke() },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = if (isSelected) "Selected" else "Not selected",
                            tint = if (isSelected) defaultPrimaryColor else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                } else if (isBlocked) {
                    // Blocked button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
                            .clickable { onBlockedClick?.invoke() }
                            .testTag("task_blocked_toggle_${task.id}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Blocked",
                            tint = DangerRed,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
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

                    if (isBlocked && blockerTitle != null) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = DangerRed,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Blocked by: $blockerTitle",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = DangerRed
                            )
                        }
                    }

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
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = DateUtils.formatTime12Hour(task.reminderTime, isArabic),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Pomodoro Count Badge
                        if (task.pomodoroCount > 0) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = "🍅 x ${task.pomodoroCount}",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
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

                    // Streak badge inline
                    if (task.isHabit && currentStreak > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "$currentStreak",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
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
                        // 1. Edit
                        DropdownMenuItem(
                            text = { Text(if (isArabic) "تعديل" else "Edit") },
                            onClick = {
                                showMenu = false
                                onEditTask()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        )

                        // 2. Mark Complete / Incomplete
                        DropdownMenuItem(
                            text = {
                                Text(
                                    if (isDone) (if (isArabic) "إلغاء الإكمال" else "Mark Incomplete")
                                    else (if (isArabic) "إكمال المهمة" else "Mark Complete")
                                )
                            },
                            onClick = {
                                showMenu = false
                                onToggleDoneToday()
                            },
                            leadingIcon = {
                                Icon(
                                    if (isDone) Icons.Default.RadioButtonUnchecked else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else SuccessGreen
                                )
                            }
                        )

                        // 3. Change Schedule
                        DropdownMenuItem(
                            text = { Text(if (isArabic) "تغيير الموعد" else "Change Schedule") },
                            onClick = {
                                showMenu = false
                                onChangeSchedule?.invoke()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        )

                        // 4. Reminder
                        DropdownMenuItem(
                            text = { Text(if (isArabic) "ضبط التنبيه" else "Reminder") },
                            onClick = {
                                showMenu = false
                                onSetReminder?.invoke()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        )

                        // 5. Duplicate
                        DropdownMenuItem(
                            text = { Text(if (isArabic) "تكرار المهمة" else "Duplicate") },
                            onClick = {
                                showMenu = false
                                onDuplicateTask?.invoke()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.ContentCopy, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        )

                        // 6. Delete
                        DropdownMenuItem(
                            text = { Text(if (isArabic) "حذف" else "Delete", color = DangerRed) },
                            onClick = {
                                showMenu = false
                                onDeleteTask()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = DangerRed)
                            }
                        )

                        // Secondary Actions
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.start_pomodoro)) },
                            onClick = {
                                showMenu = false
                                onStartPomodoro?.invoke()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.view_calendar)) },
                            onClick = {
                                showMenu = false
                                onViewCalendar()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.share_streak_card)) },
                            onClick = {
                                showMenu = false
                                onShareStreak?.invoke()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.archive_habit)) },
                            onClick = {
                                showMenu = false
                                onArchiveTask?.invoke()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Archive, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
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
        }
    }
}
