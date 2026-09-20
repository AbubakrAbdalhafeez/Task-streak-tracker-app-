package com.abubakr.taskstreak.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abubakr.taskstreak.R
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.ui.theme.DangerRed
import com.abubakr.taskstreak.ui.theme.SuccessGreen
import com.abubakr.taskstreak.ui.theme.SuccessGreenLight
import com.abubakr.taskstreak.util.DateUtils
import java.time.DayOfWeek
import java.time.LocalDate

@Composable
fun ContributionHeatmap(
    task: TaskEntity?,
    completionDates: Set<String>,
    modifier: Modifier = Modifier
) {
    val today = LocalDate.now()
    val scrollState = rememberScrollState()

    // 12 weeks = 84 days
    // Let's align to weeks so columns represent weeks and rows are Mon..Sun (or Sun..Sat)
    val daysOfWeek = listOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY,
        DayOfWeek.SUNDAY
    )

    val startDate = remember(task) {
        task?.let {
            try { DateUtils.parse(it.startDate) } catch (_: Exception) { today }
        } ?: today.minusWeeks(12)
    }
    val endDate = remember(task) {
        task?.endDate?.let {
            try { DateUtils.parse(it) } catch (_: Exception) { null }
        }
    }

    // Generate grid: 12 columns, 7 rows
    // Last cell of column 11 is today (or aligned with current day of week)
    val currentDayIndex = daysOfWeek.indexOf(today.dayOfWeek) // 0 to 6
    // The Sunday ending the current week:
    val endOfWeek = today.plusDays((6 - currentDayIndex).toLong())
    // 12 weeks back (12 * 7 = 84 days):
    val gridStart = endOfWeek.minusDays(83) // 12 full weeks

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("contribution_heatmap_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.twelve_week_heatmap),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Visual consistency like GitHub contributions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Heatmap Grid with horizontal scroll
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Day of week labels on left
                Column(
                    modifier = Modifier.padding(end = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEach { dayLabel ->
                        Box(
                            modifier = Modifier.size(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = dayLabel,
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // 12 columns of 7 days
                for (col in 0 until 12) {
                    Column(
                        modifier = Modifier.padding(horizontal = 2.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (row in 0 until 7) {
                            val dayOffset = col * 7 + row
                            val date = gridStart.plusDays(dayOffset.toLong())
                            val dateStr = DateUtils.format(date)

                            val isToday = date == today
                            val isFuture = date.isAfter(today)
                            val isPast = date.isBefore(today)

                            val isScheduled = task?.isScheduledOn(date, startDate, endDate) ?: true
                            val isCompleted = completionDates.contains(dateStr)
                            val isMissed = isPast && isScheduled && !isCompleted

                            val cellColor = when {
                                isFuture -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                isCompleted -> SuccessGreen
                                isMissed -> DangerRed.copy(alpha = 0.35f)
                                !isScheduled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                isToday && !isCompleted -> MaterialTheme.colorScheme.surfaceVariant
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }

                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(3.5.dp))
                                    .background(cellColor)
                                    .then(
                                        if (isToday) Modifier.border(
                                            1.5.dp,
                                            FlamePrimary,
                                            RoundedCornerShape(3.5.dp)
                                        )
                                        else Modifier
                                    )
                                    .testTag("heatmap_${dateStr}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Heatmap Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(SuccessGreen)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.heatmap_done), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(DangerRed.copy(alpha = 0.35f))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.heatmap_missed), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(3.dp))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.heatmap_today), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.heatmap_unscheduled), style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp))
                }
            }
        }
    }
}
