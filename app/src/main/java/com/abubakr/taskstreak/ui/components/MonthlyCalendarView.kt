package com.abubakr.taskstreak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.ui.theme.DangerRed
import com.abubakr.taskstreak.ui.theme.FlamePrimary
import com.abubakr.taskstreak.ui.theme.SuccessGreen
import com.abubakr.taskstreak.util.DateUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthlyCalendarView(
    task: TaskEntity?,
    completionDates: Set<String>, // "yyyy-MM-dd"
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val startDate = remember(task) {
        task?.let {
            try { DateUtils.parse(it.startDate) } catch (_: Exception) { today }
        } ?: today
    }
    val endDate = remember(task) {
        task?.endDate?.let {
            try { DateUtils.parse(it) } catch (_: Exception) { null }
        }
    }

    val daysOfWeek = listOf(
        DayOfWeek.SUNDAY,
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY
    )

    val firstDayOfMonth = currentMonth.atDay(1)
    val lengthOfMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeekIndex = daysOfWeek.indexOf(firstDayOfMonth.dayOfWeek)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_calendar_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Month Header with arrows
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onPreviousMonth,
                    modifier = Modifier.size(36.dp).testTag("cal_prev_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "Previous Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = currentMonth.format(DateUtils.MONTH_FORMATTER),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap past or today to toggle completion",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onNextMonth,
                    modifier = Modifier.size(36.dp).testTag("cal_next_month")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Next Month",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Day-of-week row (Sun, Mon, Tue...)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                for (dayOfWeek in daysOfWeek) {
                    Text(
                        text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3),
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days grid (up to 6 rows)
            val totalCells = ((firstDayOfWeekIndex + lengthOfMonth + 6) / 7) * 7
            val rows = totalCells / 7

            for (row in 0 until rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    for (col in 0..6) {
                        val cellIndex = row * 7 + col
                        val dayNumber = cellIndex - firstDayOfWeekIndex + 1

                        if (dayNumber in 1..lengthOfMonth) {
                            val cellDate = currentMonth.atDay(dayNumber)
                            val dateStr = DateUtils.format(cellDate)
                            val isToday = cellDate == today
                            val isPast = cellDate.isBefore(today)
                            val isFuture = cellDate.isAfter(today)

                            val isScheduled = task?.isScheduledOn(cellDate, startDate, endDate) ?: true
                            val isCompleted = completionDates.contains(dateStr)

                            val isMissed = isPast && isScheduled && !isCompleted

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(3.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        when {
                                            isCompleted -> SuccessGreen
                                            isMissed -> DangerRed.copy(alpha = 0.15f)
                                            !isScheduled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                            else -> MaterialTheme.colorScheme.surfaceVariant
                                        }
                                    )
                                    .then(
                                        if (isToday) Modifier.border(
                                            2.dp,
                                            FlamePrimary,
                                            RoundedCornerShape(8.dp)
                                        )
                                        else if (!isScheduled) Modifier.border(
                                            0.5.dp,
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            RoundedCornerShape(8.dp)
                                        )
                                        else Modifier
                                    )
                                    .clickable(enabled = !isFuture) {
                                        onDayClick(cellDate)
                                    }
                                    .testTag("cal_day_${dateStr}"),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isCompleted) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "$dayNumber",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                    }
                                } else {
                                    Text(
                                        text = "$dayNumber",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        color = when {
                                            isToday -> FlamePrimary
                                            isMissed -> DangerRed
                                            !isScheduled -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                }
                            }
                        } else {
                            // Empty cell outside current month
                            Spacer(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Calendar Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = SuccessGreen, label = "Done / منجز")
                LegendItem(color = DangerRed.copy(alpha = 0.25f), label = "Missed / فائت")
                LegendItem(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    borderColor = FlamePrimary,
                    label = "Today / اليوم"
                )
                LegendItem(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    label = "Unscheduled"
                )
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    label: String,
    borderColor: Color? = null
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
                .then(
                    if (borderColor != null) Modifier.border(1.5.dp, borderColor, RoundedCornerShape(3.dp))
                    else Modifier
                )
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
