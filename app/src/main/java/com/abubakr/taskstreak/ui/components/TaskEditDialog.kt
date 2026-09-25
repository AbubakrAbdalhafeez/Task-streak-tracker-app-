package com.abubakr.taskstreak.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.abubakr.taskstreak.R
import com.abubakr.taskstreak.data.model.CategoryEntity
import com.abubakr.taskstreak.data.model.RecurrenceType
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.util.DateUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

@Composable
fun TaskEditDialog(
    taskToEdit: TaskEntity?,
    categories: List<CategoryEntity>,
    allOtherTasks: List<TaskEntity> = emptyList(),
    initialSubtasks: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit,
    onSaveWithSubtasks: ((TaskEntity, List<String>) -> Unit)? = null
) {
    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var isHabit by remember { mutableStateOf(taskToEdit?.isHabit ?: true) }
    var blockedByTaskId by remember { mutableStateOf<Long?>(taskToEdit?.blockedByTaskId) }
    var showPrerequisiteMenu by remember { mutableStateOf(false) }
    var selectedCategory by remember {
        mutableStateOf(taskToEdit?.category ?: (categories.firstOrNull()?.name ?: "General"))
    }
    var selectedColorHex by remember {
        mutableStateOf(taskToEdit?.categoryColorHex ?: (categories.firstOrNull()?.colorHex ?: "#FF6B35"))
    }
    var recurrenceType by remember {
        mutableStateOf(taskToEdit?.recurrenceType ?: RecurrenceType.DAILY.name)
    }
    var selectedDaysOfWeek by remember {
        mutableStateOf(
            taskToEdit?.getParsedDaysOfWeek() ?: setOf(
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
            )
        )
    }
    var startDate by remember { mutableStateOf(taskToEdit?.startDate ?: DateUtils.todayString()) }
    var endDate by remember { mutableStateOf(taskToEdit?.endDate ?: "") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var reminderTime by remember { mutableStateOf(taskToEdit?.reminderTime ?: "09:00") }
    var isReminderEnabled by remember { mutableStateOf(!taskToEdit?.reminderTime.isNullOrBlank()) }
    val context = LocalContext.current
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"
    var note by remember { mutableStateOf(taskToEdit?.note ?: "") }
    var autoCompleteWithSubtasks by remember { mutableStateOf(taskToEdit?.autoCompleteWithSubtasks ?: true) }
    var subtasksList by remember { mutableStateOf(initialSubtasks) }
    var newSubtaskText by remember { mutableStateOf("") }

    var showCategoryMenu by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .testTag("task_edit_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (taskToEdit == null) stringResource(R.string.add_task) else stringResource(R.string.edit_task),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Task Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text(stringResource(R.string.task_title)) },
                    placeholder = { Text("e.g. Daily Reading, Exercise, Code...") },
                    isError = titleError,
                    supportingText = if (titleError) {
                        { Text("Task name cannot be empty") }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("task_title_input")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Habit vs One-Time Task Toggle (Feature 20)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = isHabit,
                        onClick = { isHabit = true },
                        label = { Text("🔥 " + stringResource(R.string.type_habit)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = !isHabit,
                        onClick = {
                            isHabit = false
                            recurrenceType = RecurrenceType.ONCE.name
                        },
                        label = { Text("✓ " + stringResource(R.string.type_one_time)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection
                Text(
                    text = stringResource(R.string.task_category),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { showCategoryMenu = true }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val defaultColor = MaterialTheme.colorScheme.primary
                            val color = try {
                                Color(android.graphics.Color.parseColor(selectedColorHex))
                            } catch (_: Exception) {
                                defaultColor
                            }
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = selectedCategory,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Select category",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showCategoryMenu,
                        onDismissRequest = { showCategoryMenu = false }
                    ) {
                        val defaultColor = MaterialTheme.colorScheme.primary
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        val c = try {
                                            Color(android.graphics.Color.parseColor(cat.colorHex))
                                        } catch (_: Exception) { defaultColor }
                                        Box(
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clip(CircleShape)
                                                .background(c)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(cat.name)
                                    }
                                },
                                onClick = {
                                    selectedCategory = cat.name
                                    selectedColorHex = cat.colorHex
                                    showCategoryMenu = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Recurrence Selector
                Text(
                    text = stringResource(R.string.recurrence),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        RecurrenceType.DAILY.name to stringResource(R.string.daily),
                        RecurrenceType.CUSTOM.name to stringResource(R.string.custom_days),
                        RecurrenceType.ONCE.name to stringResource(R.string.once)
                    ).forEach { (type, label) ->
                        FilterChip(
                            selected = recurrenceType == type,
                            onClick = { recurrenceType = type },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // If CUSTOM recurrence: day chips
                if (recurrenceType == RecurrenceType.CUSTOM.name) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = stringResource(R.string.select_days_label),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val dayItems = listOf(
                        DayOfWeek.SUNDAY to "Sun",
                        DayOfWeek.MONDAY to "Mon",
                        DayOfWeek.TUESDAY to "Tue",
                        DayOfWeek.WEDNESDAY to "Wed",
                        DayOfWeek.THURSDAY to "Thu",
                        DayOfWeek.FRIDAY to "Fri",
                        DayOfWeek.SATURDAY to "Sat"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        dayItems.forEach { (day, label) ->
                            val isSelected = selectedDaysOfWeek.contains(day)
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable {
                                        selectedDaysOfWeek = if (isSelected) {
                                            if (selectedDaysOfWeek.size > 1) selectedDaysOfWeek - day else selectedDaysOfWeek
                                        } else {
                                            selectedDaysOfWeek + day
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label.take(2),
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Dates: Start Date & Optional End Date (Year -> Month -> Day Picker)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Start Date Selector
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showStartDatePicker = true }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(
                                text = if (isArabic) "تاريخ البدء" else "Start Date",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = startDate.ifBlank { DateUtils.todayString() },
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    // End Date Selector (Optional)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showEndDatePicker = true }
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Text(
                                text = if (isArabic) "تاريخ الانتهاء" else "End (Optional)",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = if (endDate.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (endDate.isNotBlank()) endDate else if (isArabic) "مستمر" else "Ongoing",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = if (endDate.isNotBlank()) FontWeight.SemiBold else FontWeight.Normal
                                    ),
                                    color = if (endDate.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Reminder Row: Off or formatted Time (Click to change via TimePickerDialog)
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isReminderEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isReminderEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isReminderEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = if (isReminderEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = if (isArabic) "التنبيه" else "Reminder",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                if (isReminderEnabled) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                        modifier = Modifier
                                            .padding(top = 2.dp)
                                            .clickable {
                                                val parts = reminderTime.split(":")
                                                val initialH = parts.getOrNull(0)?.trim()?.toIntOrNull() ?: 9
                                                val initialM = parts.getOrNull(1)?.trim()?.toIntOrNull() ?: 0
                                                TimePickerDialog(
                                                    context,
                                                    { _, hourOfDay, minute ->
                                                        reminderTime = String.format(Locale.US, "%02d:%02d", hourOfDay, minute)
                                                    },
                                                    initialH,
                                                    initialM,
                                                    false // 12-hour AM/PM dialog for any time of day
                                                ).show()
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = DateUtils.formatTime12Hour(reminderTime, isArabic),
                                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Edit,
                                                contentDescription = if (isArabic) "تعديل الوقت" else "Edit Time",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = if (isArabic) "معطل (Off)" else "Off",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        Switch(
                            checked = isReminderEnabled,
                            onCheckedChange = { enabled ->
                                isReminderEnabled = enabled
                                if (enabled && reminderTime.isBlank()) {
                                    reminderTime = "09:00"
                                }
                            },
                            modifier = Modifier.testTag("switch_task_reminder")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Notes Field (Max 500 characters)
                OutlinedTextField(
                    value = note,
                    onValueChange = {
                        if (it.length <= 500) note = it
                    },
                    label = { Text(stringResource(R.string.task_notes_placeholder)) },
                    placeholder = { Text("Add instructions, checklist notes, or links...") },
                    maxLines = 4,
                    supportingText = {
                        Text("${note.length}/500", modifier = Modifier.fillMaxWidth(), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Subtasks Section
                Text(
                    text = stringResource(R.string.subtasks),
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newSubtaskText,
                        onValueChange = { newSubtaskText = it },
                        placeholder = { Text("Add a step or subtask...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (newSubtaskText.isNotBlank()) {
                                subtasksList = subtasksList + newSubtaskText.trim()
                                newSubtaskText = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(stringResource(R.string.add_subtask))
                    }
                }

                if (subtasksList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        subtasksList.forEachIndexed { index, subTitle ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${index + 1}. $subTitle",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = {
                                        subtasksList = subtasksList.filterIndexed { i, _ -> i != index }
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { autoCompleteWithSubtasks = !autoCompleteWithSubtasks }
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = autoCompleteWithSubtasks,
                            onCheckedChange = { autoCompleteWithSubtasks = it }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Auto-complete task when all subtasks are done",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Prerequisite Task (Task Dependencies - Feature 18)
                val eligibleBlockers = allOtherTasks.filter { it.id != taskToEdit?.id }
                if (eligibleBlockers.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = stringResource(R.string.prerequisite_task_label),
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    val blockerTitle = eligibleBlockers.find { it.id == blockedByTaskId }?.title ?: stringResource(R.string.none_no_dependency)
                    Box {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { showPrerequisiteMenu = true }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = blockerTitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (blockedByTaskId != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp).clickable { blockedByTaskId = null })
                        }

                        DropdownMenu(
                            expanded = showPrerequisiteMenu,
                            onDismissRequest = { showPrerequisiteMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.none_no_prerequisite)) },
                                onClick = {
                                    blockedByTaskId = null
                                    showPrerequisiteMenu = false
                                }
                            )
                            eligibleBlockers.forEach { blk ->
                                DropdownMenuItem(
                                    text = { Text(blk.title) },
                                    onClick = {
                                        blockedByTaskId = blk.id
                                        showPrerequisiteMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                                return@Button
                            }

                            val daysString = selectedDaysOfWeek.map { it.value }.sorted().joinToString(",")

                            val task = TaskEntity(
                                id = taskToEdit?.id ?: 0L,
                                title = title.trim(),
                                category = selectedCategory,
                                categoryColorHex = selectedColorHex,
                                recurrenceType = recurrenceType,
                                customDaysOfWeek = if (recurrenceType == RecurrenceType.CUSTOM.name) daysString else "1,2,3,4,5,6,7",
                                startDate = if (startDate.isNotBlank()) startDate.trim() else DateUtils.todayString(),
                                endDate = if (endDate.isNotBlank()) endDate.trim() else null,
                                reminderTime = if (isReminderEnabled && reminderTime.isNotBlank()) reminderTime.trim() else null,
                                createdAt = taskToEdit?.createdAt ?: System.currentTimeMillis(),
                                isArchived = taskToEdit?.isArchived ?: false,
                                note = if (note.isNotBlank()) note.trim() else null,
                                autoCompleteWithSubtasks = autoCompleteWithSubtasks,
                                pomodoroCount = taskToEdit?.pomodoroCount ?: 0,
                                isHabit = isHabit,
                                blockedByTaskId = blockedByTaskId,
                                reminderDays = taskToEdit?.reminderDays
                            )
                            if (onSaveWithSubtasks != null) {
                                onSaveWithSubtasks(task, subtasksList)
                            } else {
                                onSave(task)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("task_save_button")
                    ) {
                        Text(if (taskToEdit == null) stringResource(R.string.create_task) else stringResource(R.string.save_changes))
                    }
                }
            }
        }
    }

    if (showStartDatePicker) {
        YearMonthDayPickerDialog(
            title = if (isArabic) "تحديد تاريخ البدء" else "Select Start Date",
            initialDate = startDate,
            allowClear = false,
            onDateSelected = { selectedDate ->
                startDate = selectedDate
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    if (showEndDatePicker) {
        YearMonthDayPickerDialog(
            title = if (isArabic) "تحديد تاريخ الانتهاء" else "Select End Date",
            initialDate = if (endDate.isNotBlank()) endDate else startDate,
            allowClear = true,
            onDateSelected = { selectedDate ->
                endDate = selectedDate
            },
            onClearDate = {
                endDate = ""
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}
