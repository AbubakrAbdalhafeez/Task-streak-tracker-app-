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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.ui.components.AppErrorBoundary
import com.abubakr.taskstreak.ui.screens.AchievementsScreen
import com.abubakr.taskstreak.ui.screens.AdvancedBackupScreen
import com.abubakr.taskstreak.ui.screens.ArchiveScreen
import com.abubakr.taskstreak.ui.screens.CalendarAnalyticsScreen
import com.abubakr.taskstreak.ui.screens.CategoryManagementScreen
import com.abubakr.taskstreak.ui.screens.CommunityHubScreen
import com.abubakr.taskstreak.ui.screens.HabitChainsScreen
import com.abubakr.taskstreak.ui.screens.HomeScreen
import com.abubakr.taskstreak.ui.screens.OnboardingScreen
import com.abubakr.taskstreak.ui.screens.PomodoroScreen
import com.abubakr.taskstreak.ui.screens.PomodoroSettingsScreen
import com.abubakr.taskstreak.ui.screens.SettingsScreen
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

enum class SubScreen {
    NONE,
    ARCHIVE,
    CATEGORIES,
    HABIT_CHAINS,
    ADVANCED_BACKUP,
    COMMUNITY_HUB,
    ONBOARDING,
    POMODORO_SETTINGS
}

@Composable
fun MainApp(
    viewModel: StreakViewModel,
    modifier: Modifier = Modifier
) {
    val onboardingCompleted by viewModel.preferences.onboardingCompleted.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(MainTab.TASKS) }
    var currentSubScreen by remember {
        mutableStateOf(if (!onboardingCompleted) SubScreen.ONBOARDING else SubScreen.NONE)
    }
    var appError by remember { mutableStateOf<Throwable?>(null) }
    var calendarSelectedTask by remember { mutableStateOf<TaskEntity?>(null) }
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.preferences.soundEnabled.collectAsStateWithLifecycle()
    val hapticsEnabled by viewModel.preferences.hapticsEnabled.collectAsStateWithLifecycle()

    AppErrorBoundary(
        error = appError,
        onRestartApp = {
            appError = null
            currentSubScreen = SubScreen.NONE
            selectedTab = MainTab.TASKS
        }
    ) {
        if (currentSubScreen != SubScreen.NONE) {
            when (currentSubScreen) {
                SubScreen.ONBOARDING -> {
                    OnboardingScreen(
                        onFinish = {
                            viewModel.preferences.setOnboardingCompleted(true)
                            currentSubScreen = SubScreen.NONE
                        }
                    )
                }
                SubScreen.COMMUNITY_HUB -> {
                    CommunityHubScreen(
                        viewModel = viewModel,
                        onBack = { currentSubScreen = SubScreen.NONE }
                    )
                }
                SubScreen.ARCHIVE -> {
                    ArchiveScreen(
                        viewModel = viewModel,
                        onBack = { currentSubScreen = SubScreen.NONE }
                    )
                }
                SubScreen.CATEGORIES -> {
                    CategoryManagementScreen(
                        viewModel = viewModel,
                        onBack = { currentSubScreen = SubScreen.NONE }
                    )
                }
                SubScreen.HABIT_CHAINS -> {
                    HabitChainsScreen(
                        viewModel = viewModel,
                        onBack = { currentSubScreen = SubScreen.NONE }
                    )
                }
                SubScreen.ADVANCED_BACKUP -> {
                    AdvancedBackupScreen(
                        viewModel = viewModel,
                        onBack = { currentSubScreen = SubScreen.NONE }
                    )
                }
                SubScreen.POMODORO_SETTINGS -> {
                    PomodoroSettingsScreen(
                        viewModel = viewModel,
                        onBack = { currentSubScreen = SubScreen.NONE }
                    )
                }
                SubScreen.NONE -> {}
            }
            return@AppErrorBoundary
        }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .testTag("bottom_navigation_bar")
            ) {
                val isArabic = LocalConfiguration.current.locales[0].language == "ar"
                MainTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val tabLabel = if (isArabic) tab.arabicTitle else tab.title
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tabLabel,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tabLabel,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
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
                            },
                            onNavigateToHabitChains = {
                                currentSubScreen = SubScreen.HABIT_CHAINS
                            },
                            onNavigateToArchive = {
                                currentSubScreen = SubScreen.ARCHIVE
                            },
                            onNavigateToCategories = {
                                currentSubScreen = SubScreen.CATEGORIES
                            }
                        )
                    }
                    MainTab.POMODORO -> {
                        PomodoroScreen(
                            viewModel = viewModel,
                            onNavigateToSettings = {
                                currentSubScreen = SubScreen.POMODORO_SETTINGS
                            }
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
                        SettingsScreen(
                            viewModel = viewModel,
                            onNavigateToArchive = { currentSubScreen = SubScreen.ARCHIVE },
                            onNavigateToCategories = { currentSubScreen = SubScreen.CATEGORIES },
                            onNavigateToHabitChains = { currentSubScreen = SubScreen.HABIT_CHAINS },
                            onNavigateToAdvancedBackup = { currentSubScreen = SubScreen.ADVANCED_BACKUP },
                            onNavigateToCommunityHub = { currentSubScreen = SubScreen.COMMUNITY_HUB },
                            onReplayOnboarding = { currentSubScreen = SubScreen.ONBOARDING }
                        )
                    }
                }
            }
        }
    }
    }
}
