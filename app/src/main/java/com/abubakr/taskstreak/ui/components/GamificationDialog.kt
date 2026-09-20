package com.abubakr.taskstreak.ui.components

import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abubakr.taskstreak.R
import com.abubakr.taskstreak.data.model.GamificationCalculator
import com.abubakr.taskstreak.data.model.Quest
import com.abubakr.taskstreak.ui.viewmodel.StreakViewModel

@Composable
fun GamificationDialog(
    viewModel: StreakViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"
    val totalXp by viewModel.totalXp.collectAsState()
    val userLevel by viewModel.userLevel.collectAsState()
    val streakShields by viewModel.streakShields.collectAsState()
    val overallStats by viewModel.overallStats.collectAsState()

    val quests = remember(overallStats) {
        GamificationCalculator.getDailyQuests(
            completedTodayCount = overallStats.completedTodayCount,
            pomodoroSessions = 1,
            currentBestStreak = overallStats.bestOverallStreak
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(userLevel.badgeIcon, fontSize = 28.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(userLevel.localizedTitle(isArabic), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        if (isArabic) "المستوى ${userLevel.level} • $totalXp إجمالي النقاط" else "Level ${userLevel.level} • $totalXp Total XP",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // XP Progress Bar
                LinearProgressIndicator(
                    progress = { userLevel.progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp),
                    color = Color(0xFFFFB300),
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    if (isArabic) "${userLevel.xpInCurrentLevel} / 100 نقطة إلى المستوى ${userLevel.level + 1}" else "${userLevel.xpInCurrentLevel} / 100 XP to Level ${userLevel.level + 1}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Spacer(Modifier.height(16.dp))

                // Streak Shield Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    if (isArabic) "دروع الحماية: $streakShields" else "Streak Shields: $streakShields",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    if (isArabic) "حماية سلسلة العادات من الانقطاع" else "Protects against broken streaks",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (streakShields > 0) {
                            OutlinedButton(
                                onClick = {
                                    val used = viewModel.useStreakShield()
                                    if (used) {
                                        Toast.makeText(
                                            context,
                                            if (isArabic) "🛡️ تم تفعيل درع الحماية! تم إنقاذ سلسلتك." else "🛡️ Streak Shield Activated! Streak saved.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                modifier = Modifier.testTag("use_shield_button")
                            ) {
                                Text(if (isArabic) "استخدام" else "Use", fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                Text(
                    if (isArabic) "مهام وتحديات اليوم" else "Daily & Weekly Quests",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(quests, key = { it.id }) { quest ->
                        QuestCard(
                            quest = quest,
                            isArabic = isArabic,
                            onClaim = {
                                viewModel.addXp(quest.xpReward.toLong())
                                Toast.makeText(
                                    context,
                                    if (isArabic) "+${quest.xpReward} نقطة مكتسبة!" else "+${quest.xpReward} XP Earned!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss, modifier = Modifier.testTag("close_gamification_button")) {
                Text(if (isArabic) "إغلاق" else "Close")
            }
        }
    )
}

@Composable
private fun QuestCard(
    quest: Quest,
    isArabic: Boolean = false,
    onClaim: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(quest.localizedTitle(isArabic), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text(quest.localizedDescription(isArabic), fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (quest.progress.toFloat() / quest.target.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = if (quest.isCompleted) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            if (quest.isCompleted) {
                Button(
                    onClick = onClaim,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("claim_quest_${quest.id}")
                ) {
                    Text("+${quest.xpReward} XP", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Text("${quest.progress}/${quest.target}", fontSize = 12.sp, color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}
