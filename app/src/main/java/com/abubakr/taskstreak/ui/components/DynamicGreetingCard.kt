package com.abubakr.taskstreak.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abubakr.taskstreak.ui.theme.DangerRed
import com.abubakr.taskstreak.ui.theme.FlamePrimary
import com.abubakr.taskstreak.ui.theme.FlameSecondary
import com.abubakr.taskstreak.ui.theme.SuccessGreen
import com.abubakr.taskstreak.ui.theme.WarningOrange
import com.abubakr.taskstreak.util.OverallStats
import java.time.LocalTime

@Composable
fun DynamicGreetingCard(
    overallStats: OverallStats,
    modifier: Modifier = Modifier
) {
    val currentHour = LocalTime.now().hour
    val (greetingText, greetingSub, timeIcon) = when {
        currentHour in 5..11 -> Triple("Good morning", "صباح النشاط والهمة", "☀️")
        currentHour in 12..16 -> Triple("Good afternoon", "مساء الإنجاز والتركيز", "🌤️")
        currentHour in 17..21 -> Triple("Good evening", "مساء الخير والتألق", "🌅")
        else -> Triple("Good night", "ليلة هادئة واستعداد للغد", "🌙")
    }

    val pendingCount = (overallStats.totalScheduledTodayCount - overallStats.completedTodayCount).coerceAtLeast(0)
    val allCompletedToday = overallStats.totalScheduledTodayCount > 0 && pendingCount == 0

    val statusMessage = when {
        overallStats.totalScheduledTodayCount == 0 -> "No scheduled tasks for today. Relax or plan ahead!"
        allCompletedToday -> "Great job! All scheduled tasks finished! 🎉"
        else -> "You have $pendingCount task${if (pendingCount > 1) "s" else ""} waiting today"
    }

    val atRiskTasks = overallStats.activeTasksWithStreakAtRisk

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dynamic_greeting_card")
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant,
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = timeIcon,
                            fontSize = 24.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(
                                text = greetingText,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = greetingSub,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (overallStats.bestOverallStreak > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = FlamePrimary.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FlamePrimary.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = FlamePrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${overallStats.bestOverallStreak}d best",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = FlamePrimary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (allCompletedToday) SuccessGreen.copy(alpha = 0.12f)
                            else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (allCompletedToday) Icons.Default.CheckCircle else Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = if (allCompletedToday) SuccessGreen else WarningOrange,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Fire motivation if streak is strong
                if (overallStats.bestOverallStreak >= 3) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔥 You're on fire! Keep the momentum going!",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = FlameSecondary
                        )
                    }
                }

                // Streak at risk warning
                AnimatedVisibility(visible = atRiskTasks.isNotEmpty()) {
                    val firstAtRisk = atRiskTasks.firstOrNull()
                    if (firstAtRisk != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(DangerRed.copy(alpha = 0.12f))
                                .border(1.dp, DangerRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Warning",
                                tint = DangerRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "⚠️ Streak at risk! \"${firstAtRisk.title}\" is waiting today!",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = DangerRed
                            )
                        }
                    }
                }
            }
        }
    }
}
