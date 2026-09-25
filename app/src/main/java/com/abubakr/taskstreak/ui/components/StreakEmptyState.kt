package com.abubakr.taskstreak.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abubakr.taskstreak.ui.theme.InfoBlue
import com.abubakr.taskstreak.ui.theme.SuccessGreen

enum class EmptyStateType {
    NO_TASKS,
    NO_COMPLETIONS_TODAY,
    NO_ACHIEVEMENTS,
    NO_CATEGORIES,
    SEARCH_NO_RESULTS,
    ALL_CAUGHT_UP
}

@Composable
fun StreakEmptyState(
    type: EmptyStateType,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    secondaryActionText: String? = null,
    onSecondaryAction: (() -> Unit)? = null
) {
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary

    val (icon, title, titleAr, subtitle, subtitleAr, actionText, actionTextAr, buttonColor) = when (type) {
        EmptyStateType.SEARCH_NO_RESULTS -> EmptyStateInfo(
            icon = Icons.Default.SearchOff,
            title = "No Matching Habits",
            titleAr = "لا توجد نتائج مطابقة",
            subtitle = "Try adjusting your search terms or filter selection.",
            subtitleAr = "جرّب تغيير كلمات البحث أو إعادة ضبط التصنيفات.",
            actionText = "Clear Search",
            actionTextAr = "مسح البحث",
            buttonColor = secondaryColor
        )
        EmptyStateType.ALL_CAUGHT_UP -> EmptyStateInfo(
            icon = Icons.Default.CheckCircle,
            title = "All Caught Up! 🎉",
            titleAr = "أنجزت جميع مهام اليوم! 🎉",
            subtitle = "Every pending habit is finished for today. Rest and celebrate your consistency!",
            subtitleAr = "اكتملت جميع عاداتك لليوم. استمتع بإنجازك واسترح!",
            actionText = "Add Bonus Habit",
            actionTextAr = "إضافة عادة إضافية",
            buttonColor = SuccessGreen
        )
        EmptyStateType.NO_TASKS -> EmptyStateInfo(
            icon = Icons.Default.LocalFireDepartment,
            title = "No Active Habits Yet",
            titleAr = "لا توجد عادات نشطة حالياً",
            subtitle = "Every great journey begins with a single daily step. Build your first habit now!",
            subtitleAr = "ابدأ ببناء عادتك الأولى الآن واستمر عليها يومياً لتشاهد أثرها.",
            actionText = "Create Habit",
            actionTextAr = "إضافة عادة",
            buttonColor = primaryColor
        )
        EmptyStateType.NO_COMPLETIONS_TODAY -> EmptyStateInfo(
            icon = Icons.Default.CheckCircle,
            title = "Ready for Today's Wins?",
            titleAr = "جاهز لإنجاز مهام اليوم؟",
            subtitle = "You have habits waiting to be checked off. Keep your streak alive today!",
            subtitleAr = "لديك عادات بانتظار إتمامها. حافظ على سلسلتك مستمرة اليوم!",
            actionText = "Review Habits",
            actionTextAr = "مراجعة العادات",
            buttonColor = InfoBlue
        )
        EmptyStateType.NO_ACHIEVEMENTS -> EmptyStateInfo(
            icon = Icons.Default.EmojiEvents,
            title = "Trophies Await!",
            titleAr = "الجوائز بانتظارك!",
            subtitle = "Keep completing habits and hitting streak milestones to unlock epic badges.",
            subtitleAr = "استمر بإكمال العادات وتحقيق الأرقام القياسية لفتح الأوسمة.",
            actionText = "Start A Streak",
            actionTextAr = "ابدأ سلسلة الآن",
            buttonColor = primaryColor
        )
        EmptyStateType.NO_CATEGORIES -> EmptyStateInfo(
            icon = Icons.Default.Category,
            title = "No Custom Categories",
            titleAr = "لا توجد تصنيفات مخصصة",
            subtitle = "Organize habits into routines like Morning, Health, Work, or Learning.",
            subtitleAr = "قسّم عاداتك إلى تصنيفات مثل الصباح، الرياضة، العمل، أو القراءة.",
            actionText = "Add Category",
            actionTextAr = "إضافة تصنيف",
            buttonColor = SuccessGreen
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("empty_state_${type.name.lowercase()}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Illustrated Circular Glow
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(
                                buttonColor.copy(alpha = 0.3f),
                                buttonColor.copy(alpha = 0.05f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = buttonColor,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isArabic) titleAr else title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isArabic) subtitleAr else subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            if (onAction != null || onSecondaryAction != null) {
                Spacer(modifier = Modifier.height(22.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onAction != null) {
                        Button(
                            onClick = onAction,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = buttonColor),
                            modifier = Modifier.testTag("btn_empty_state_action")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) actionTextAr else actionText, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    if (onSecondaryAction != null && secondaryActionText != null) {
                        OutlinedButton(
                            onClick = onSecondaryAction,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("btn_empty_state_secondary")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(secondaryActionText, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

private data class EmptyStateInfo(
    val icon: ImageVector,
    val title: String,
    val titleAr: String,
    val subtitle: String,
    val subtitleAr: String,
    val actionText: String,
    val actionTextAr: String,
    val buttonColor: Color
)
