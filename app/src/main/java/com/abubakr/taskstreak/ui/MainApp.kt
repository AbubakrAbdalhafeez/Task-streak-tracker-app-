package com.abubakr.taskstreak.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.ui.screens.AchievementsScreen
import com.abubakr.taskstreak.ui.screens.CalendarAnalyticsScreen
import com.abubakr.taskstreak.ui.screens.HomeScreen
import com.abubakr.taskstreak.ui.screens.PomodoroScreen
import com.abubakr.taskstreak.ui.screens.SettingsScreen
import com.abubakr.taskstreak.ui.theme.FlamePrimary
import com.abubakr.taskstreak.ui.viewmodel.StreakViewModel

enum class MainTab(
    val title: String,
    val arabicTitle: String,
    val icon: ImageVector,
    val testTag: String
) {
    TASKS("Tasks", "المهام", Icons.Default.CheckCircle, "nav_tasks"),
    POMODORO("Focus", "التركيز", Icons.Default.Timer, "nav_pomodoro"),
    CALENDAR("Calendar", "التقويم", Icons.Default.CalendarMonth, "nav_calendar"),
    ACHIEVEMENTS("Badges", "الإنجازات", Icons.Default.EmojiEvents, "nav_achievements"),
    SETTINGS("Settings", "الإعدادات", Icons.Default.Settings, "nav_settings")
}

@Composable
fun MainApp(
    viewModel: StreakViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(MainTab.TASKS) }
    var calendarSelectedTask by remember { mutableStateOf<TaskEntity?>(null) }
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.preferences.soundEnabled.collectAsStateWithLifecycle()
    val hapticsEnabled by viewModel.preferences.hapticsEnabled.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
            ) {
                MainTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = FlamePrimary,
                            indicatorColor = FlamePrimary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        },
        modifier = modifier.fillMaxSize()
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Crossfade(targetState = selectedTab, label = "tab_crossfade") { tab ->
                when (tab) {
                    MainTab.TASKS -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onNavigateToCalendarForTask = { task ->
                                calendarSelectedTask = task
                                selectedTab = MainTab.CALENDAR
                            },
                            onStartPomodoroForTask = { task ->
                                selectedTab = MainTab.POMODORO
                            }
                        )
                    }
                    MainTab.POMODORO -> {
                        PomodoroScreen(
                            tasks = tasks,
                            onCompletePomodoro = { taskId ->
                                viewModel.incrementPomodoro(taskId)
                            },
                            soundEnabled = soundEnabled,
                            hapticsEnabled = hapticsEnabled
                        )
                    }
                    MainTab.CALENDAR -> {
                        CalendarAnalyticsScreen(
                            viewModel = viewModel,
                            initialSelectedTask = calendarSelectedTask
                        )
                    }
                    MainTab.ACHIEVEMENTS -> {
                        AchievementsScreen(viewModel = viewModel)
                    }
                    MainTab.SETTINGS -> {
                        SettingsScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
