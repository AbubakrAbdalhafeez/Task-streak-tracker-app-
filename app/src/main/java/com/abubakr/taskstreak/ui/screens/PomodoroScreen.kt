package com.abubakr.taskstreak.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abubakr.taskstreak.R
import com.abubakr.taskstreak.data.model.FocusSessionMode
import com.abubakr.taskstreak.ui.theme.DangerRed
import com.abubakr.taskstreak.ui.theme.InfoBlue
import com.abubakr.taskstreak.ui.theme.SuccessGreen
import com.abubakr.taskstreak.ui.theme.WarningOrange
import com.abubakr.taskstreak.ui.viewmodel.StreakViewModel
import com.abubakr.taskstreak.util.SoundAndHapticHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PomodoroScreen(
    viewModel: StreakViewModel,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isArabic = LocalConfiguration.current.locales[0].language == "ar"

    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val isRunning by viewModel.focusIsRunning.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.focusRemainingSeconds.collectAsStateWithLifecycle()
    val targetSeconds by viewModel.focusTargetSeconds.collectAsStateWithLifecycle()
    val phase by viewModel.focusPhase.collectAsStateWithLifecycle()
    val selectedTaskId by viewModel.focusSelectedTaskId.collectAsStateWithLifecycle()
    val countdown by viewModel.focusAutoStartCountdown.collectAsStateWithLifecycle()
    val stats by viewModel.focusDailyStats.collectAsStateWithLifecycle()

    val sessionModeStr by viewModel.preferences.focusSessionMode.collectAsStateWithLifecycle()
    val partialCreditEnabled by viewModel.preferences.partialCreditEnabled.collectAsStateWithLifecycle()
    val dailyMinutesGoal by viewModel.preferences.focusDailyMinutesGoal.collectAsStateWithLifecycle()
    val dailySessionsGoal by viewModel.preferences.focusDailySessionsGoal.collectAsStateWithLifecycle()
    val lastDurationMinutes by viewModel.preferences.focusLastDurationMinutes.collectAsStateWithLifecycle()

    val soundEnabled by viewModel.preferences.soundEnabled.collectAsStateWithLifecycle()
    val hapticsEnabled by viewModel.preferences.hapticsEnabled.collectAsStateWithLifecycle()

    val currentMode = runCatching { FocusSessionMode.valueOf(sessionModeStr) }.getOrDefault(FocusSessionMode.CLASSIC)
    val selectedTask = tasks.find { it.id == selectedTaskId }
    var showTaskPicker by remember { mutableStateOf(false) }

    // Progress calculation
    val progress = if (targetSeconds > 0) {
        (remainingSeconds.toFloat() / targetSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec,
        label = "pomodoro_smooth_progress"
    )

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    // Vibrant phase-aware gradients
    val phaseBrush = when (phase) {
        StreakViewModel.FocusPhase.WORK -> Brush.linearGradient(
            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.75f))
        )
        StreakViewModel.FocusPhase.SHORT_BREAK -> Brush.linearGradient(
            listOf(SuccessGreen, Color(0xFF059669))
        )
        StreakViewModel.FocusPhase.LONG_BREAK -> Brush.linearGradient(
            listOf(InfoBlue, Color(0xFF6366F1))
        )
    }

    val phaseColor = when (phase) {
        StreakViewModel.FocusPhase.WORK -> MaterialTheme.colorScheme.primary
        StreakViewModel.FocusPhase.SHORT_BREAK -> SuccessGreen
        StreakViewModel.FocusPhase.LONG_BREAK -> InfoBlue
    }

    val phaseTitle = when (phase) {
        StreakViewModel.FocusPhase.WORK -> if (isArabic) "جلسة عمل وتركيز" else "Work Focus"
        StreakViewModel.FocusPhase.SHORT_BREAK -> if (isArabic) "استراحة قصيرة" else "Short Break"
        StreakViewModel.FocusPhase.LONG_BREAK -> if (isArabic) "استراحة طويلة" else "Long Break"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isArabic) "مؤقت بومودورو للتركيز" else "Focus & Pomodoro",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("btn_pomodoro_settings")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = if (isArabic) "إعدادات المؤقت" else "Timer Settings",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
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
            contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Countdown Auto-Start Banner (if active)
            if (countdown != null) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(6.dp, RoundedCornerShape(16.dp))
                            .testTag("card_autostart_countdown"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    progress = { (countdown ?: 10) / 10f },
                                    modifier = Modifier.size(32.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 3.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isArabic) "البدء التلقائي خلال $countdown ثوانٍ..." else "Auto-starting in ${countdown}s...",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = if (isArabic) "استعد للجلسة التالية" else "Prepare for next phase",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                            }
                            OutlinedButton(
                                onClick = { viewModel.cancelCountdown() },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("btn_cancel_countdown")
                            ) {
                                Text(if (isArabic) "إلغاء" else "Cancel")
                            }
                        }
                    }
                }
            }

            // Quick Mode Selector Tabs
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        FocusSessionMode.values().forEach { mode ->
                            val isSelected = currentMode == mode
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { viewModel.setFocusSessionMode(mode) }
                                    .padding(horizontal = 2.dp)
                                    .testTag("tab_mode_${mode.name.lowercase()}")
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (mode) {
                                            FocusSessionMode.CLASSIC -> if (isArabic) "كلاسيكي" else "Classic"
                                            FocusSessionMode.DEEP_WORK -> if (isArabic) "عميق" else "Deep Work"
                                            FocusSessionMode.CUSTOM -> if (isArabic) "مخصص" else "Custom"
                                        },
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Quick Preset Buttons & Slider (5 - 120m)
            if (phase == StreakViewModel.FocusPhase.WORK && !isRunning) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().testTag("card_duration_selector"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isArabic) "مدة الجلسة" else "Session Duration",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "$lastDurationMinutes min",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            // Quick buttons (15, 25, 45, 60)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(15, 25, 45, 60).forEach { mins ->
                                    val isSelected = lastDurationMinutes == mins
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.setFocusCustomDuration(mins) },
                                        label = { Text("${mins}m") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                                            selectedLabelColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).testTag("chip_preset_${mins}")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            // Continuous Slider from 5 to 120 mins
                            Slider(
                                value = lastDurationMinutes.toFloat(),
                                onValueChange = { viewModel.setFocusCustomDuration(it.toInt()) },
                                valueRange = 5f..120f,
                                steps = 22,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.fillMaxWidth().testTag("slider_duration")
                            )
                        }
                    }
                }
            }

            // Linked Task Selector Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showTaskPicker = true }
                        .testTag("pomodoro_task_selector")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) "العادة أو المهمة المرتبطة" else "Linked Habit / Task",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = selectedTask?.title ?: (if (isArabic) "اختر عادة للربط بها..." else "Select a habit to link..."),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (selectedTask != null) {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "🍅 x ${selectedTask.pomodoroCount}",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showTaskPicker,
                            onDismissRequest = { showTaskPicker = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (isArabic) "بدون ارتباط" else "None (General Focus)") },
                                onClick = {
                                    viewModel.setFocusSelectedTask(null)
                                    showTaskPicker = false
                                }
                            )
                            tasks.forEach { task ->
                                DropdownMenuItem(
                                    text = { Text("${task.title} (🍅 x ${task.pomodoroCount})") },
                                    onClick = {
                                        viewModel.setFocusSelectedTask(task.id)
                                        showTaskPicker = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Modern Timer Circular Display with Shadows & Progress
            item {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(280.dp)
                        .padding(12.dp)
                ) {
                    // Outer background ring
                    CircularProgressIndicator(
                        progress = { 1f },
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                        strokeWidth = 14.dp,
                    )
                    // Active animated progress ring
                    CircularProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxSize(),
                        color = phaseColor,
                        strokeWidth = 14.dp,
                        strokeCap = StrokeCap.Round
                    )

                    // Inner Timer Text & State Indicator
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = phaseColor.copy(alpha = 0.15f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = phaseTitle,
                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                color = phaseColor,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = timeFormatted,
                            style = MaterialTheme.typography.displayMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 2.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (isRunning)
                                (if (isArabic) "جلسة نشطة..." else "Session in progress...")
                            else
                                (if (isArabic) "جاهز للبدء" else "Ready to focus"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Primary Timer Action Buttons (Play / Pause / Stop / Skip)
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset / Abandon Button
                    IconButton(
                        onClick = {
                            if (isRunning) {
                                viewModel.stopAndLogFocus(abandoned = true)
                            } else {
                                viewModel.resetFocusTimer()
                            }
                            SoundAndHapticHelper.playTickFeedback(context, soundEnabled, hapticsEnabled)
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("pomodoro_reset_button")
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Stop else Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.pomodoro_reset),
                            tint = if (isRunning) DangerRed else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Main Start / Pause FAB-style Button
                    Button(
                        onClick = {
                            if (isRunning) {
                                viewModel.pauseFocusTimer()
                            } else {
                                viewModel.startFocusTimer()
                            }
                            SoundAndHapticHelper.playTickFeedback(context, soundEnabled, hapticsEnabled)
                        },
                        modifier = Modifier
                            .height(62.dp)
                            .width(170.dp)
                            .shadow(8.dp, RoundedCornerShape(31.dp))
                            .testTag("pomodoro_toggle_button"),
                        shape = RoundedCornerShape(31.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isRunning) DangerRed else MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isRunning)
                                stringResource(R.string.pomodoro_pause)
                            else
                                stringResource(R.string.pomodoro_start),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    // Skip current phase button
                    IconButton(
                        onClick = {
                            viewModel.skipCurrentPhase()
                            SoundAndHapticHelper.playTickFeedback(context, soundEnabled, hapticsEnabled)
                        },
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("pomodoro_skip_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = if (isArabic) "تخطي" else "Skip",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Stats Card (Today's Progress & Session breakdown)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                        .testTag("card_focus_stats"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isArabic) "إحصائيات إنجاز اليوم" else "Today's Focus Stats",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (stats.currentStreakDays > 0) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = WarningOrange.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.LocalFireDepartment, contentDescription = null, tint = WarningOrange, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${stats.currentStreakDays}d streak",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = WarningOrange
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Daily Minutes Goal Progress Bar
                        val minutesProgress = (stats.totalMinutesToday.toFloat() / dailyMinutesGoal.toFloat()).coerceIn(0f, 1f)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (isArabic) "الهدف اليومي: ${stats.totalMinutesToday} / $dailyMinutesGoal دقيقة"
                                       else "Daily Goal: ${stats.totalMinutesToday} / ${dailyMinutesGoal}m",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${(minutesProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { minutesProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = MaterialTheme.colorScheme.primary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Grid of 4 counters: Completed, Partial, Abandoned, Minutes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Completed (Green)
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${stats.completedSessionsToday}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = SuccessGreen
                                )
                                Text(
                                    text = if (isArabic) "مكتملة" else "Completed",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Partial (Info Blue / Amber)
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${stats.partialSessionsToday}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = InfoBlue
                                )
                                Text(
                                    text = if (isArabic) "جزئية" else "Partial",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Abandoned (Danger Red)
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${stats.abandonedSessionsToday}",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = DangerRed
                                )
                                Text(
                                    text = if (isArabic) "فاشلة" else "Abandoned",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            // Total Time
                            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${stats.totalMinutesToday}m",
                                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = if (isArabic) "إجمالي الوقت" else "Total Time",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
