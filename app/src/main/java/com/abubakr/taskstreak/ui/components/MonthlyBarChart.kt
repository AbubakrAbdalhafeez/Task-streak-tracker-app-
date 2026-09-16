package com.abubakr.taskstreak.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abubakr.taskstreak.data.model.CompletionLogEntity
import com.abubakr.taskstreak.ui.theme.FlamePrimary
import com.abubakr.taskstreak.ui.theme.FlameSecondary
import com.abubakr.taskstreak.util.DateUtils
import java.time.LocalDate
import java.time.Month
import java.time.Year
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun MonthlyBarChart(
    logs: List<CompletionLogEntity>,
    targetYear: Int = Year.now().value,
    modifier: Modifier = Modifier
) {
    val currentMonth = LocalDate.now().monthValue // 1 to 12
    val currentYear = LocalDate.now().year

    // Compute completions per month for targetYear
    val monthCounts = remember(logs, targetYear) {
        val counts = IntArray(12) { 0 }
        for (log in logs) {
            try {
                val date = DateUtils.parse(log.date)
                if (date.year == targetYear) {
                    counts[date.monthValue - 1]++
                }
            } catch (_: Exception) {}
        }
        counts
    }

    val maxCount = (monthCounts.maxOrNull() ?: 0).coerceAtLeast(1)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_barchart_card"),
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
                        text = "Monthly Progress / الإنجاز الشهري ($targetYear)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Task completions breakdown across 12 months",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "${monthCounts.sum()} total",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = FlamePrimary
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Bar chart row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                for (m in 1..12) {
                    val count = monthCounts[m - 1]
                    val isCurrentMonth = (targetYear == currentYear && m == currentMonth)
                    val fraction = (count.toFloat() / maxCount.toFloat()).coerceIn(0.04f, 1f)

                    val animatedHeight by animateFloatAsState(
                        targetValue = fraction,
                        label = "bar_height_$m"
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        if (count > 0) {
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Bold),
                                color = if (isCurrentMonth) FlamePrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                        }

                        // The Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.65f)
                                .fillMaxHeight(animatedHeight)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    if (isCurrentMonth) {
                                        Brush.verticalGradient(listOf(FlamePrimary, FlameSecondary))
                                    } else if (count > 0) {
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                                                MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                                            )
                                        )
                                    } else {
                                        Brush.verticalGradient(
                                            listOf(
                                                MaterialTheme.colorScheme.surfaceVariant,
                                                MaterialTheme.colorScheme.surfaceVariant
                                            )
                                        )
                                    }
                                )
                                .then(
                                    if (isCurrentMonth) Modifier.border(
                                        1.dp,
                                        FlamePrimary,
                                        RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                    )
                                    else Modifier
                                )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Month label
                        val monthName = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(3)
                        Text(
                            text = monthName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 9.sp,
                                fontWeight = if (isCurrentMonth) FontWeight.Bold else FontWeight.Normal
                            ),
                            color = if (isCurrentMonth) FlamePrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
