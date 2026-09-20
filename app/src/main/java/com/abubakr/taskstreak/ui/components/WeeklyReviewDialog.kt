package com.abubakr.taskstreak.ui.components

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.ui.theme.SuccessGreen
import com.abubakr.taskstreak.util.DateUtils
import com.abubakr.taskstreak.util.OverallStats
import com.abubakr.taskstreak.util.TaskStreakStats
import java.time.LocalDate

@Composable
fun WeeklyReviewDialog(
    tasks: List<TaskEntity>,
    taskStatsMap: Map<Long, TaskStreakStats>,
    overallStats: OverallStats,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"
    val today = LocalDate.now()
    val past7Days = (0..6).map { today.minusDays(it.toLong()) }

    // Calculate this week's completions
    val activeTasks = tasks.filter { !it.isArchived }
    val bestStreaks = activeTasks.sortedByDescending { taskStatsMap[it.id]?.currentStreak ?: 0 }.take(3)
    val needsAttention = activeTasks.filter { (taskStatsMap[it.id]?.completionRate ?: 0f) < 50f }.take(3)

    val totalExpectedCompletions = (activeTasks.size * 7).coerceAtLeast(1)
    val totalDoneThisWeek = activeTasks.sumOf { task ->
        val stats = taskStatsMap[task.id]
        stats?.totalCompletions?.coerceAtMost(7) ?: 0
    }
    val weekRate = ((totalDoneThisWeek.toFloat() / totalExpectedCompletions.toFloat()) * 100f).coerceIn(0f, 100f)

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.fillMaxWidth()
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isArabic) "المراجعة الأسبوعية" else "Weekly Review",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isArabic) "ملخص إنجاز العادات لهذا الأسبوع" else "Your 7-day habit performance",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weekly Performance Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isArabic) "معدل الإنجاز الأسبوعي" else "Weekly Completion Rate",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${weekRate.toInt()}%",
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { weekRate / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Highlights / Best streaks
                Text(
                    text = if (isArabic) "🏆 أفضل السلاسل هذا الأسبوع" else "🏆 Top Streaks This Week",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                bestStreaks.forEach { task ->
                    val stats = taskStatsMap[task.id]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "${stats?.currentStreak ?: 0} ${if (isArabic) "أيام" else "days"}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                if (needsAttention.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = if (isArabic) "⚠️ بحاجة إلى اهتمام" else "⚠️ Needs Attention",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    needsAttention.forEach { task ->
                        Text(
                            text = "• ${task.title} ${if (isArabic) "(معدل الإنجاز منخفض)" else "(Completion rate is low)"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Coaching Adjustment Suggestion
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.TipsAndUpdates,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isArabic) {
                                if (weekRate >= 80f) "استمرارية رائعة هذا الأسبوع! فكّر في رفع هدفك اليومي أو إضافة عادة جديدة."
                                else if (weekRate >= 50f) "جهد جيد! جرّب ضبط تذكيرات صباحية للعادات التي تفوتك غالباً."
                                else "تذكير لطيف: العادات الصغيرة تصنع فارقاً هائلاً. ركّز على عادة أو اثنتين غداً."
                            } else {
                                if (weekRate >= 80f) "Outstanding consistency this week! Consider raising your daily goal or adding a stretch habit."
                                else if (weekRate >= 50f) "Solid effort! Try setting custom morning reminders for habits you frequently miss."
                                else "A gentle reminder: Small habits build massive results. Focus on just 1 or 2 core habits tomorrow."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val shareText = if (isArabic) {
                                "📅 مراجعة السلسلة الأسبوعية:\n" +
                                        "✨ إنجاز الأسبوع: ${weekRate.toInt()}%\n" +
                                        "🔥 إجمالي العادات النشطة: ${activeTasks.size}\n" +
                                        "🏆 أفضل سلسلة: ${bestStreaks.firstOrNull()?.let { taskStatsMap[it.id]?.currentStreak } ?: 0} أيام\n\n" +
                                        "بناء الاستمرارية مع TaskStreak! 🚀"
                            } else {
                                "📅 Weekly Streak Review:\n" +
                                        "✨ Week Completion: ${weekRate.toInt()}%\n" +
                                        "🔥 Total Active Habits: ${activeTasks.size}\n" +
                                        "🏆 Best Streak: ${bestStreaks.firstOrNull()?.let { taskStatsMap[it.id]?.currentStreak } ?: 0} days\n\n" +
                                        "Building consistency with TaskStreak! 🚀"
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, if (isArabic) "مشاركة المراجعة الأسبوعية" else "Share Weekly Review"))
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isArabic) "مشاركة" else "Share")
                    }

                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(if (isArabic) "تم" else "Done")
                    }
                }
            }
        }
    }
}
