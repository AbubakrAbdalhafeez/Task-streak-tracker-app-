package com.abubakr.taskstreak.ui.components

import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abubakr.taskstreak.data.model.RecurrenceType
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.util.DateUtils
import java.time.LocalDate
import java.util.Locale

@Composable
fun QuickScheduleDialog(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onSaveSchedule: (newStartDate: String, recurrenceType: String, customDays: String) -> Unit
) {
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"
    var selectedRecurrence by remember { mutableStateOf(task.recurrenceType) }
    var startDateText by remember { mutableStateOf(task.startDate) }
    var showDatePicker by remember { mutableStateOf(false) }
    var customDays by remember { mutableStateOf(task.customDaysOfWeek) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = if (isArabic) "تغيير موعد المهمة" else "Change Schedule",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )

                // Quick Date Presets
                Text(
                    text = if (isArabic) "التاريخ / البداية" else "Date / Start",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val todayStr = DateUtils.todayString()
                    val tomorrowStr = LocalDate.now().plusDays(1).toString()

                    FilterChip(
                        selected = startDateText == todayStr,
                        onClick = { startDateText = todayStr },
                        label = { Text(if (isArabic) "اليوم" else "Today") }
                    )
                    FilterChip(
                        selected = startDateText == tomorrowStr,
                        onClick = { startDateText = tomorrowStr },
                        label = { Text(if (isArabic) "غداً" else "Tomorrow") }
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showDatePicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isArabic) "تاريخ الاستحقاق / البدء" else "Due Date / Start",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = startDateText,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Recurrence
                Text(
                    text = if (isArabic) "نوع التكرار" else "Recurrence",
                    style = MaterialTheme.typography.labelMedium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedRecurrence == RecurrenceType.DAILY.name,
                        onClick = { selectedRecurrence = RecurrenceType.DAILY.name },
                        label = { Text(if (isArabic) "يومي" else "Daily") }
                    )
                    FilterChip(
                        selected = selectedRecurrence == RecurrenceType.ONCE.name,
                        onClick = { selectedRecurrence = RecurrenceType.ONCE.name },
                        label = { Text(if (isArabic) "مرة واحدة" else "Once") }
                    )
                    FilterChip(
                        selected = selectedRecurrence == RecurrenceType.CUSTOM.name,
                        onClick = { selectedRecurrence = RecurrenceType.CUSTOM.name },
                        label = { Text(if (isArabic) "مخصص" else "Custom") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveSchedule(startDateText, selectedRecurrence, customDays)
                    onDismiss()
                },
                modifier = Modifier.testTag("btn_save_quick_schedule")
            ) {
                Text(if (isArabic) "حفظ" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )

    if (showDatePicker) {
        YearMonthDayPickerDialog(
            title = if (isArabic) "تحديد تاريخ الاستحقاق" else "Select Due Date",
            initialDate = startDateText,
            allowClear = false,
            onDateSelected = { selected ->
                startDateText = selected
            },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun QuickReminderDialog(
    task: TaskEntity,
    onDismiss: () -> Unit,
    onSaveReminder: (newReminderTime: String?) -> Unit
) {
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"
    val context = LocalContext.current
    var reminderTime by remember { mutableStateOf(task.reminderTime ?: "09:00") }
    var isEnabled by remember { mutableStateOf(task.reminderTime != null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
        },
        title = {
            Text(
                text = if (isArabic) "ضبط التنبيه" else "Set Reminder",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Compact Reminder Toggle Row
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                        else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
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
                                        if (isEnabled) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                                    contentDescription = null,
                                    tint = if (isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
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

                                if (isEnabled) {
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
                                                    false
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
                            checked = isEnabled,
                            onCheckedChange = { enabled ->
                                isEnabled = enabled
                                if (enabled && reminderTime.isBlank()) {
                                    reminderTime = "09:00"
                                }
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveReminder(if (isEnabled) reminderTime else null)
                    onDismiss()
                },
                modifier = Modifier.testTag("btn_save_quick_reminder")
            ) {
                Text(if (isArabic) "حفظ" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isArabic) "إلغاء" else "Cancel")
            }
        }
    )
}
