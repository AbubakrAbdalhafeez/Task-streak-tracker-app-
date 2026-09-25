package com.abubakr.taskstreak.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abubakr.taskstreak.data.model.FocusAutoStartType
import com.abubakr.taskstreak.data.model.FocusSessionMode
import com.abubakr.taskstreak.ui.viewmodel.StreakViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroSettingsScreen(
    viewModel: StreakViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"
    val partialCredit by viewModel.preferences.partialCreditEnabled.collectAsStateWithLifecycle()
    val sessionModeStr by viewModel.preferences.focusSessionMode.collectAsStateWithLifecycle()
    val autoStartTypeStr by viewModel.preferences.focusAutoStartType.collectAsStateWithLifecycle()
    val oneMinWarning by viewModel.preferences.focusOneMinuteWarningEnabled.collectAsStateWithLifecycle()
    val completionNotif by viewModel.preferences.focusCompletionNotificationEnabled.collectAsStateWithLifecycle()

    val customWorkMins by viewModel.preferences.focusCustomWorkMinutes.collectAsStateWithLifecycle()
    val customShortBreakMins by viewModel.preferences.focusCustomShortBreakMinutes.collectAsStateWithLifecycle()
    val customLongBreakMins by viewModel.preferences.focusCustomLongBreakMinutes.collectAsStateWithLifecycle()
    val customCycles by viewModel.preferences.focusCustomCyclesBeforeLongBreak.collectAsStateWithLifecycle()

    val dailyMinutesGoal by viewModel.preferences.focusDailyMinutesGoal.collectAsStateWithLifecycle()
    val dailySessionsGoal by viewModel.preferences.focusDailySessionsGoal.collectAsStateWithLifecycle()

    val currentMode = runCatching { FocusSessionMode.valueOf(sessionModeStr) }.getOrDefault(FocusSessionMode.CLASSIC)
    val currentAutoStart = runCatching { FocusAutoStartType.valueOf(autoStartTypeStr) }.getOrDefault(FocusAutoStartType.MANUAL)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isArabic) "إعدادات جلسات التركيز" else "Focus & Pomodoro Settings",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("btn_back_pomodoro_settings")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isArabic) "رجوع" else "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 12.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Partial Credit Logic
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_partial_credit_settings"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "احتساب الدقائق الجزئية" else "Partial Session Credit",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (isArabic)
                                        "تسجيل الوقت المنجز حتى لو لم تكتمل الجلسة بالكامل، ووسمها كجلسة جزئية بدلاً من اعتبارها فاشلة بصفر دقائق."
                                    else
                                        "Keep recorded minutes even if session stops early. Marks the session as 'Partial' instead of failing.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = partialCredit,
                                onCheckedChange = { viewModel.preferences.setPartialCreditEnabled(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("switch_partial_credit")
                            )
                        }
                    }
                }
            }

            // Card 2: Session Mode Selection
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_session_mode_settings"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "نمط الجلسات الافتراضي" else "Session Mode",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        FocusSessionMode.values().forEach { mode ->
                            val isSelected = currentMode == mode
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.setFocusSessionMode(mode) },
                                label = {
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Text(
                                            text = if (isArabic) mode.titleAr else mode.titleEn,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        val subtitle = when (mode) {
                                            FocusSessionMode.CLASSIC -> if (isArabic) "25 دقيقة عمل، 5 دقائق استراحة، بعد 4 دورات استراحة 15 دقيقة" else "25m focus, 5m break, 15m long break after 4 cycles"
                                            FocusSessionMode.DEEP_WORK -> if (isArabic) "90 دقيقة عمل عميق، 15 دقيقة استراحة، بعد دورتين استراحة 20 دقيقة" else "90m deep focus, 15m break, 20m long break after 2 cycles"
                                            FocusSessionMode.CUSTOM -> if (isArabic) "تخصيص كامل للأوقات وعدد الدورات حسب رغبتك" else "Fully customized durations and cycle counts"
                                        }
                                        Text(
                                            text = subtitle,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = if (isSelected) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .testTag("chip_mode_${mode.name.lowercase()}")
                            )
                        }

                        // If Custom mode is selected, show customizable sliders
                        if (currentMode == FocusSessionMode.CUSTOM) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (isArabic) "تخصيص أوقات النمط المخصص:" else "Customize Custom Mode Parameters:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Custom Work Duration
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isArabic) "مدة جلسة العمل" else "Work Duration",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "$customWorkMins min",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = customWorkMins.toFloat(),
                                onValueChange = { viewModel.preferences.setFocusCustomWorkMinutes(it.toInt()) },
                                valueRange = 5f..120f,
                                steps = 22,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            // Custom Short Break
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isArabic) "مدة الاستراحة القصيرة" else "Short Break Duration",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "$customShortBreakMins min",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = customShortBreakMins.toFloat(),
                                onValueChange = { viewModel.preferences.setFocusCustomShortBreakMinutes(it.toInt()) },
                                valueRange = 1f..30f,
                                steps = 28,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            // Custom Long Break
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isArabic) "مدة الاستراحة الطويلة" else "Long Break Duration",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "$customLongBreakMins min",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = customLongBreakMins.toFloat(),
                                onValueChange = { viewModel.preferences.setFocusCustomLongBreakMinutes(it.toInt()) },
                                valueRange = 5f..60f,
                                steps = 10,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            // Cycles before long break
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isArabic) "عدد الجلسات قبل الاستراحة الطويلة" else "Cycles before Long Break",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = "$customCycles cycles",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = customCycles.toFloat(),
                                onValueChange = { viewModel.preferences.setFocusCustomCyclesBeforeLongBreak(it.toInt()) },
                                valueRange = 2f..8f,
                                steps = 5,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }

            // Card 3: Auto-Start Behavior & Countdown
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_autostart_settings"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "خيارات البدء التلقائي" else "Auto-Start Options",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isArabic)
                                "عند تفعيل البدء التلقائي، يظهر عد تنازلي مدته 10 ثوانٍ مع خيار إلغائه قبل بدء الجلسة التالية."
                            else
                                "When enabled, a smooth 10s countdown allows you to prepare or cancel before the next phase starts automatically.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        FocusAutoStartType.values().forEach { type ->
                            val isSelected = currentAutoStart == type
                            FilterChip(
                                selected = isSelected,
                                onClick = { viewModel.preferences.setFocusAutoStartType(type.name) },
                                label = {
                                    Text(
                                        text = if (isArabic) type.titleAr else type.titleEn,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .testTag("chip_autostart_${type.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Card 4: Smart Notifications
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_focus_notifications_settings"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "الإشعارات الذكية للجلسات" else "Smart Focus Notifications",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))

                        // 1-minute warning switch
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "تنبيه قبل دقيقة من النهاية" else "1-Minute Remaining Warning",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isArabic) "إشعار هادئ لإنهاء الفكرة أو المهمة الحالية" else "Gentle alert to wind down current thought",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = oneMinWarning,
                                onCheckedChange = { viewModel.preferences.setFocusOneMinuteWarningEnabled(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("switch_one_min_warning")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Session & Break End Notifications
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) "إشعارات انتهاء الجلسة والاستراحة" else "Completion & Break Alerts",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (isArabic) "تنبيه عند اكتمال جلسة التركيز أو انتهاء وقت الاستراحة" else "Alert when work finishes or break ends",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = completionNotif,
                                onCheckedChange = { viewModel.preferences.setFocusCompletionNotificationEnabled(it) },
                                colors = SwitchDefaults.colors(checkedThumbColor = MaterialTheme.colorScheme.primary),
                                modifier = Modifier.testTag("switch_completion_notif")
                            )
                        }
                    }
                }
            }

            // Card 5: Daily Focus Goals
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().testTag("card_daily_goals_settings"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isArabic) "الأهداف اليومية للتركيز" else "Daily Focus Goals",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isArabic) "الهدف اليومي للدقائق" else "Daily Target Minutes",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "$dailyMinutesGoal min",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = dailyMinutesGoal.toFloat(),
                            onValueChange = { viewModel.preferences.setFocusDailyGoals(dailySessionsGoal, it.toInt()) },
                            valueRange = 20f..360f,
                            steps = 33,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isArabic) "الهدف اليومي للجلسات" else "Daily Target Sessions",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "$dailySessionsGoal sessions",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = dailySessionsGoal.toFloat(),
                            onValueChange = { viewModel.preferences.setFocusDailyGoals(it.toInt(), dailyMinutesGoal) },
                            valueRange = 1f..12f,
                            steps = 10,
                            colors = SliderDefaults.colors(
                                thumbColor = MaterialTheme.colorScheme.primary,
                                activeTrackColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }
    }
}
