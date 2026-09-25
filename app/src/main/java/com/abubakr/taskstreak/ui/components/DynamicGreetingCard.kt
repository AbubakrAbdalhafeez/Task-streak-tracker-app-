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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abubakr.taskstreak.ui.theme.DangerRed
import com.abubakr.taskstreak.ui.theme.SuccessGreen
import com.abubakr.taskstreak.ui.theme.WarningOrange
import com.abubakr.taskstreak.util.OverallStats
import java.time.LocalTime

@Composable
fun DynamicGreetingCard(
    overallStats: OverallStats,
    modifier: Modifier = Modifier
) {
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"
    val currentHour = LocalTime.now().hour
    val (greetingText, greetingSub, timeIcon) = when {
        currentHour in 5..11 -> Triple(
            if (isArabic) "صباح الخير" else "Good morning",
            if (isArabic) "صباح النشاط والهمة" else "Ready for a productive day",
            "☀️"
        )
        currentHour in 12..16 -> Triple(
            if (isArabic) "مساء الخير" else "Good afternoon",
            if (isArabic) "مساء الإنجاز والتركيز" else "Keep up the great momentum",
            "🌤️"
        )
        currentHour in 17..21 -> Triple(
            if (isArabic) "مساء الخير" else "Good evening",
            if (isArabic) "مساء التألق والاسترخاء" else "Review your daily wins",
            "🌅"
        )
        else -> Triple(
            if (isArabic) "تصبح على خير" else "Good night",
            if (isArabic) "ليلة هادئة واستعداد للغد" else "Rest well and recharge",
            "🌙"
        )
    }

    val pendingCount = (overallStats.totalScheduledTodayCount - overallStats.completedTodayCount).coerceAtLeast(0)
    val allCompletedToday = overallStats.totalScheduledTodayCount > 0 && pendingCount == 0

    val statusMessage = when {
        overallStats.totalScheduledTodayCount == 0 -> if (isArabic) "لا توجد مهام مجدولة لليوم. استرخِ أو خطط للمستقبل!" else "No scheduled tasks for today. Relax or plan ahead!"
        allCompletedToday -> if (isArabic) "عمل رائع! تم إنجاز جميع مهام اليوم! 🎉" else "Great job! All scheduled tasks finished! 🎉"
        else -> if (isArabic) "لديك $pendingCount من المهام المتبقية اليوم" else "You have $pendingCount task${if (pendingCount > 1) "s" else ""} waiting today"
    }

    val atRiskTasks = overallStats.activeTasksWithStreakAtRisk

    val brandBlue = Color(0xFF2563EB)
    val brandBlueLight = Color(0xFF3B82F6)
    val brandBlueDark = Color(0xFF1E3A8A)
    val brandBlueSoft = Color(0xFFDBEAFE)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("dynamic_greeting_card")
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            brandBlueDark,
                            brandBlue
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
                            fontSize = 26.sp,
                            modifier = Modifier.padding(end = 10.dp)
                        )
                        Column {
                            Text(
                                text = greetingText,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = greetingSub,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFBFDBFE)
                            )
                        }
                    }

                    if (overallStats.bestOverallStreak > 0) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Streak",
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${overallStats.bestOverallStreak}${if (isArabic) " يوم كأفضل سلسلة" else "d best"}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status banner (Frosted Glass Blue Pill)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.16f))
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (allCompletedToday) Icons.Default.CheckCircle else Icons.Default.WbSunny,
                        contentDescription = null,
                        tint = if (allCompletedToday) Color(0xFF4ADE80) else Color(0xFFFDE047),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = statusMessage,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = Color.White
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
                            text = if (isArabic) "🔥 سلسلتك قوية ومشتعلة! حافظ على زخمك!" else "🔥 You're on fire! Keep the momentum going!",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = Color(0xFF93C5FD)
                        )
                    }
                }

                // Streak at risk warning (Prominent Red Banner with subtle shadow, clear contrast, and ⚠️ icon)
                AnimatedVisibility(visible = atRiskTasks.isNotEmpty()) {
                    val firstAtRisk = atRiskTasks.firstOrNull()
                    if (firstAtRisk != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp)),
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFFDC2626), // Vibrant accessible red with high contrast against blue card
                            shadowElevation = 4.dp,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFCA5A5).copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 11.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "⚠️",
                                    fontSize = 18.sp,
                                    modifier = Modifier.padding(end = 8.dp)
                                )
                                Text(
                                    text = if (isArabic) "تنبيه السلسلة: \"${firstAtRisk.title}\" مهددة بالانقطاع اليوم!" else "Streak Alert: \"${firstAtRisk.title}\" is at risk today!",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
