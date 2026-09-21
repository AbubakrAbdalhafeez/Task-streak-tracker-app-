package com.abubakr.taskstreak.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import com.abubakr.taskstreak.data.model.Achievement
import com.abubakr.taskstreak.data.model.TaskTemplate
import com.abubakr.taskstreak.ui.components.AchievementUnlockDialog
import com.abubakr.taskstreak.ui.components.ConfettiEffect
import com.abubakr.taskstreak.ui.components.DataInsightsCard
import com.abubakr.taskstreak.ui.components.EmptyStateType
import com.abubakr.taskstreak.ui.components.GamificationDialog
import com.abubakr.taskstreak.ui.components.MilestoneCelebrationDialog
import com.abubakr.taskstreak.ui.components.QrSyncDialog
import com.abubakr.taskstreak.ui.components.SocialPassportDialog
import com.abubakr.taskstreak.ui.components.StreakEmptyState
import com.abubakr.taskstreak.ui.components.TemplatePickerDialog
import com.abubakr.taskstreak.ui.components.VoiceInputDialog
import com.abubakr.taskstreak.ui.components.WeeklyReviewDialog
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abubakr.taskstreak.R
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.ui.components.AddCategoryDialog
import com.abubakr.taskstreak.ui.components.DailyGoalCard
import com.abubakr.taskstreak.ui.components.DynamicGreetingCard
import com.abubakr.taskstreak.ui.components.TaskCard
import com.abubakr.taskstreak.ui.components.TaskEditDialog
import com.abubakr.taskstreak.ui.theme.DangerRed
import com.abubakr.taskstreak.ui.viewmodel.StreakViewModel
import com.abubakr.taskstreak.util.SoundAndHapticHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: StreakViewModel,
    onNavigateToCalendarForTask: (TaskEntity) -> Unit,
    onStartPomodoroForTask: ((TaskEntity) -> Unit)? = null,
    onNavigateToHabitChains: (() -> Unit)? = null,
    onNavigateToArchive: (() -> Unit)? = null,
    onNavigateToCategories: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val taskStatsMap by viewModel.taskStatsMap.collectAsStateWithLifecycle()
    val overallStats by viewModel.overallStats.collectAsStateWithLifecycle()
    val dailyGoal by viewModel.preferences.dailyGoal.collectAsStateWithLifecycle()
    val subtasksMap by viewModel.subtasksMap.collectAsStateWithLifecycle()
    val soundEnabled by viewModel.preferences.soundEnabled.collectAsStateWithLifecycle()
    val hapticsEnabled by viewModel.preferences.hapticsEnabled.collectAsStateWithLifecycle()

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val typeFilter by viewModel.typeFilter.collectAsStateWithLifecycle()
    val isSelectionMode by viewModel.isSelectionMode.collectAsStateWithLifecycle()
    val selectedTaskIds by viewModel.selectedTaskIds.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val quote = remember { viewModel.getDailyQuote() }

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }
    var showBulkDeleteConfirm by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    var showConfetti by remember { mutableStateOf(false) }
    var showTemplatePicker by remember { mutableStateOf(false) }
    var showWeeklyReview by remember { mutableStateOf(false) }
    var showQrSync by remember { mutableStateOf(false) }
    var qrExportJson by remember { mutableStateOf<String?>(null) }
    var showVoiceInput by remember { mutableStateOf(false) }
    var showGamificationDialog by remember { mutableStateOf(false) }
    var streakTaskToShare by remember { mutableStateOf<TaskEntity?>(null) }
    var milestoneCelebrationData by remember { mutableStateOf<Pair<TaskEntity, Int>?>(null) }
    var unlockedAchievement by remember { mutableStateOf<Achievement?>(null) }

    val filteredTasks = remember(tasks, selectedCategory, taskStatsMap, searchQuery, statusFilter, typeFilter) {
        var list = if (selectedCategory == null) tasks else tasks.filter { it.category == selectedCategory }
        if (searchQuery.isNotBlank()) {
            list = list.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        (it.note?.contains(searchQuery, ignoreCase = true) == true)
            }
        }
        if (typeFilter == "HABIT") {
            list = list.filter { it.isHabit }
        } else if (typeFilter == "TASK") {
            list = list.filter { !it.isHabit }
        }
        if (statusFilter == "PENDING") {
            list = list.filter { task -> taskStatsMap[task.id]?.isCompletedToday != true }
        } else if (statusFilter == "COMPLETED") {
            list = list.filter { task -> taskStatsMap[task.id]?.isCompletedToday == true }
        }
        list.sortedWith(
            compareBy<TaskEntity> { task ->
                val stats = taskStatsMap[task.id]
                if (stats?.isCompletedToday == true) 1 else 0
            }.thenByDescending { it.createdAt }
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbarHostState) },
            floatingActionButton = {
                if (!isSelectionMode) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FloatingActionButton(
                            onClick = { showTemplatePicker = true },
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("fab_templates")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Templates",
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Templates",
                                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }

                        FloatingActionButton(
                            onClick = { showAddTaskDialog = true },
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.testTag("fab_add_task")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Task",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // App Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocalFireDepartment,
                                    contentDescription = "Fire logo",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Streak Tracker",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 22.sp
                                    ),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = "Build habits day by day",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Header Actions (Voice, Level, Chains, Archive, Weekly Review, QR Sync)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showVoiceInput = true },
                                modifier = Modifier.testTag("btn_voice_input")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice Input",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = { showGamificationDialog = true },
                                modifier = Modifier.testTag("btn_gamification")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MilitaryTech,
                                    contentDescription = "Level & Shields",
                                    tint = Color(0xFFFFB300)
                                )
                            }
                            IconButton(
                                onClick = { onNavigateToHabitChains?.invoke() },
                                modifier = Modifier.testTag("btn_habit_chains")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Link,
                                    contentDescription = "Habit Chains",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { onNavigateToArchive?.invoke() },
                                modifier = Modifier.testTag("btn_archive")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Archive,
                                    contentDescription = "Archived Habits",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(
                                onClick = { showWeeklyReview = true },
                                modifier = Modifier.testTag("btn_weekly_review")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Assessment,
                                    contentDescription = "Weekly Review",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            IconButton(
                                onClick = {
                                    viewModel.exportData { json ->
                                        qrExportJson = json
                                        showQrSync = true
                                    }
                                },
                                modifier = Modifier.testTag("btn_qr_sync")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = "QR Sync",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

            // Data Insights Card (Feature 33)
            item {
                val productivityInsight by viewModel.productivityInsights.collectAsStateWithLifecycle()
                DataInsightsCard(insight = productivityInsight)
            }

            // Motivational Quote Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "\"${quote.first}\"",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    lineHeight = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "— ${quote.second}",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Selection Mode Action Bar
            if (isSelectionMode) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.clearSelection() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close selection")
                                }
                                Text(
                                    text = "${selectedTaskIds.size} Selected",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { viewModel.selectAll(filteredTasks.map { it.id }) }) {
                                    Icon(Icons.Default.SelectAll, contentDescription = "Select all")
                                }
                                IconButton(onClick = { viewModel.bulkCompleteSelected() }) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "Bulk complete",
                                        tint = com.abubakr.taskstreak.ui.theme.SuccessGreen
                                    )
                                }
                                IconButton(onClick = { showBulkDeleteConfirm = true }) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = "Bulk delete",
                                        tint = DangerRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Search Bar & Filter Row
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.searchQuery.value = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search habits & notes...") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp)
                    )

                    // Quick status and type filter chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = statusFilter == "ALL",
                            onClick = { viewModel.statusFilter.value = "ALL" },
                            label = { Text("All Status") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = statusFilter == "PENDING",
                            onClick = { viewModel.statusFilter.value = "PENDING" },
                            label = { Text("Pending") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = statusFilter == "COMPLETED",
                            onClick = { viewModel.statusFilter.value = "COMPLETED" },
                            label = { Text("Completed") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = com.abubakr.taskstreak.ui.theme.SuccessGreen,
                                selectedLabelColor = Color.White
                            )
                        )

                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))

                        FilterChip(
                            selected = typeFilter == "ALL",
                            onClick = { viewModel.typeFilter.value = "ALL" },
                            label = { Text("All Types") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = typeFilter == "HABIT",
                            onClick = {
                                viewModel.typeFilter.value = if (typeFilter == "HABIT") "ALL" else "HABIT"
                            },
                            label = { Text("🔥 Habits") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            )
                        )
                        FilterChip(
                            selected = typeFilter == "TASK",
                            onClick = {
                                viewModel.typeFilter.value = if (typeFilter == "TASK") "ALL" else "TASK"
                            },
                            label = { Text("✓ One-Time Tasks") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
            }

            // Dynamic Greeting Card
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    DynamicGreetingCard(overallStats = overallStats)
                }
            }

            // Daily Goal Card
            item {
                Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                    DailyGoalCard(
                        dailyGoal = dailyGoal,
                        completedToday = overallStats.completedTodayCount,
                        onGoalChange = { viewModel.setDailyGoal(it) }
                    )
                }
            }

            // Category Filter Chips
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val defaultColor = MaterialTheme.colorScheme.primary
                        // "All" chip
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.setSelectedCategory(null) },
                            label = { Text("All (${tasks.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = defaultColor,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_all")
                        )

                        // Category chips
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat.name
                            val catColor = try {
                                Color(android.graphics.Color.parseColor(cat.colorHex))
                            } catch (_: Exception) { defaultColor }

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.setSelectedCategory(if (isSelected) null else cat.name)
                                },
                                label = { Text(cat.name) },
                                leadingIcon = {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(catColor)
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = catColor,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("filter_cat_${cat.name}")
                            )
                        }

                        // Add Category Chip
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showAddCategoryDialog = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add category",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Category",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Tasks Section Title
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedCategory == null) stringResource(R.string.all_tasks) else "$selectedCategory Tasks",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${filteredTasks.size} task${if (filteredTasks.size == 1) "" else "s"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Empty state if no tasks
            if (filteredTasks.isEmpty()) {
                item {
                    val emptyType = when {
                        searchQuery.isNotBlank() -> EmptyStateType.SEARCH_NO_RESULTS
                        statusFilter == "PENDING" -> EmptyStateType.ALL_CAUGHT_UP
                        tasks.isEmpty() -> EmptyStateType.NO_TASKS
                        else -> EmptyStateType.NO_TASKS
                    }
                    StreakEmptyState(
                        type = emptyType,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        onAction = { showAddTaskDialog = true }
                    )
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    val blockerTask = task.blockedByTaskId?.let { bId -> tasks.find { it.id == bId } }
                    val isBlocked = blockerTask != null && (taskStatsMap[blockerTask.id]?.isCompletedToday != true)

                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        TaskCard(
                            task = task,
                            stats = taskStatsMap[task.id],
                            subtasks = subtasksMap[task.id] ?: emptyList(),
                            isBlocked = isBlocked,
                            blockerTitle = blockerTask?.title,
                            onBlockedClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar("🔒 Complete \"${blockerTask?.title}\" first!")
                                }
                            },
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedTaskIds.contains(task.id),
                            onToggleSelect = { viewModel.toggleSelection(task.id) },
                            onLongClick = { viewModel.startSelection(task.id) },
                            onToggleDoneToday = {
                                val stats = taskStatsMap[task.id]
                                val wasDone = stats?.isCompletedToday == true
                                viewModel.toggleTaskToday(task.id)
                                if (!wasDone) {
                                    showConfetti = true
                                    SoundAndHapticHelper.playSuccessFeedback(context, soundEnabled, hapticsEnabled)
                                    val newStreak = (stats?.currentStreak ?: 0) + 1
                                    if (newStreak in listOf(7, 30, 100, 365)) {
                                        milestoneCelebrationData = Pair(task, newStreak)
                                    }
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Completed: ${task.title} 🔥",
                                            actionLabel = "Undo"
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.undoToggleTaskCompletion()
                                        }
                                    }
                                }
                            },
                            onEditTask = { taskToEdit = task },
                            onDeleteTask = {
                                viewModel.deleteTaskWithUndo(task) { deletedTitle ->
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "Deleted \"$deletedTitle\"",
                                            actionLabel = "Undo"
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.undoDelete()
                                        }
                                    }
                                }
                            },
                            onViewCalendar = { onNavigateToCalendarForTask(task) },
                            onToggleSubtask = { sub, isDone ->
                                viewModel.toggleSubtask(sub, isDone)
                                SoundAndHapticHelper.playTickFeedback(context, soundEnabled, hapticsEnabled)
                            },
                            onStartPomodoro = {
                                onStartPomodoroForTask?.invoke(task)
                            },
                            onArchiveTask = {
                                viewModel.archiveTask(task.id)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Archived \"${task.title}\"")
                                }
                            },
                            onSyncCalendar = {
                                viewModel.syncTaskToCalendar(context, task)
                            },
                            onShareStreak = {
                                streakTaskToShare = task
                            }
                        )
                    }
                }
            }
        }
    }

    // Voice Input Dialog (Feature 35)
    if (showVoiceInput) {
        VoiceInputDialog(
            onDismiss = { showVoiceInput = false },
            onSaveTask = { parsed ->
                val newTask = TaskEntity(
                    title = parsed.title,
                    category = parsed.category,
                    recurrenceType = parsed.recurrenceType.name,
                    reminderTime = parsed.reminderTime,
                    isHabit = true,
                    createdAt = System.currentTimeMillis()
                )
                viewModel.saveTaskWithSubtasks(newTask, emptyList())
                showVoiceInput = false
                scope.launch {
                    snackbarHostState.showSnackbar("Created task: \"${parsed.title}\" 🎙️")
                }
            }
        )
    }

    // Gamification & Shields Dialog (Feature 34)
    if (showGamificationDialog) {
        GamificationDialog(
            viewModel = viewModel,
            onDismiss = { showGamificationDialog = false }
        )
    }

    // Social Passport Dialog (Feature 37)
    streakTaskToShare?.let { task ->
        val stats = taskStatsMap[task.id]
        SocialPassportDialog(
            task = task,
            streakDays = stats?.currentStreak ?: 0,
            onDismiss = { streakTaskToShare = null }
        )
    }

    // Add / Edit Task Dialog
    if (showAddTaskDialog || taskToEdit != null) {
        val currentSubtaskTitles = remember(taskToEdit, subtasksMap) {
            taskToEdit?.let { subtasksMap[it.id]?.map { s -> s.title } } ?: emptyList()
        }
        TaskEditDialog(
            taskToEdit = taskToEdit,
            categories = categories,
            allOtherTasks = tasks,
            initialSubtasks = currentSubtaskTitles,
            onDismiss = {
                showAddTaskDialog = false
                taskToEdit = null
            },
            onSave = { task ->
                viewModel.saveTask(task)
                showAddTaskDialog = false
                taskToEdit = null
            },
            onSaveWithSubtasks = { task, subs ->
                viewModel.saveTaskWithSubtasks(task, subs)
                showAddTaskDialog = false
                taskToEdit = null
            }
        )
    }

    // Delete Confirmation Dialog with Undo
    if (taskToDelete != null) {
        AlertDialog(
            onDismissRequest = { taskToDelete = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = DangerRed
                )
            },
            title = { Text(stringResource(R.string.delete_task_confirm_title)) },
            text = {
                Text(
                    stringResource(R.string.delete_task_confirm_message, taskToDelete?.title ?: "")
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val target = taskToDelete
                        taskToDelete = null
                        if (target != null) {
                            viewModel.deleteTaskWithUndo(target) { name ->
                                scope.launch {
                                    val res = snackbarHostState.showSnackbar(
                                        message = "Deleted \"$name\"",
                                        actionLabel = "Undo",
                                        duration = androidx.compose.material3.SnackbarDuration.Short
                                    )
                                    if (res == SnackbarResult.ActionPerformed) {
                                        viewModel.undoDelete()
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text(stringResource(R.string.delete), color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Bulk Delete Confirmation Dialog
    if (showBulkDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showBulkDeleteConfirm = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Bulk Delete",
                    tint = DangerRed
                )
            },
            title = { Text("Delete ${selectedTaskIds.size} Tasks?") },
            text = {
                Text("Are you sure you want to delete all selected tasks?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.bulkDeleteSelected()
                        showBulkDeleteConfirm = false
                    }
                ) {
                    Text("Delete All", color = DangerRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBulkDeleteConfirm = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add Category Dialog
    if (showAddCategoryDialog) {
        AddCategoryDialog(
            onDismiss = { showAddCategoryDialog = false },
            onSave = { name, colorHex, iconName ->
                viewModel.addCategory(name, colorHex, iconName)
                showAddCategoryDialog = false
            }
        )
    }

    // Template Picker Dialog (Feature 16)
    if (showTemplatePicker) {
        TemplatePickerDialog(
            onDismiss = { showTemplatePicker = false },
            onSelectTemplate = { template ->
                showTemplatePicker = false
                val newTask = TaskEntity(
                    title = template.title,
                    category = template.category,
                    categoryColorHex = template.categoryColorHex,
                    recurrenceType = template.recurrenceType,
                    customDaysOfWeek = template.customDaysOfWeek,
                    reminderTime = template.reminderTime,
                    isHabit = template.isHabit,
                    note = template.note,
                    createdAt = System.currentTimeMillis()
                )
                viewModel.saveTaskWithSubtasks(newTask, template.defaultSubtasks)
                scope.launch {
                    snackbarHostState.showSnackbar("Added habit: ${template.title} ✨")
                }
            }
        )
    }

    // Weekly Review Dialog (Feature 21)
    if (showWeeklyReview) {
        WeeklyReviewDialog(
            tasks = tasks,
            taskStatsMap = taskStatsMap,
            overallStats = overallStats,
            onDismiss = { showWeeklyReview = false }
        )
    }

    // QR Sync Dialog (Feature 14)
    if (showQrSync) {
        QrSyncDialog(
            exportJsonString = qrExportJson ?: "",
            onDismiss = {
                showQrSync = false
                qrExportJson = null
            },
            onImportJson = { json ->
                viewModel.importData(json) { success, msg ->
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            if (success) "Sync successful! ✅" else "Sync failed: $msg"
                        )
                    }
                }
            }
        )
    }

    // Confetti Effect (Feature 15)
    ConfettiEffect(
        isVisible = showConfetti,
        onAnimationEnd = { showConfetti = false }
    )

    // Milestone Celebration Dialog (Feature 43)
    milestoneCelebrationData?.let { (task, streakCount) ->
        MilestoneCelebrationDialog(
            milestoneDays = streakCount,
            task = task,
            onDismiss = { milestoneCelebrationData = null }
        )
    }

    // Achievement Unlock Dialog (Feature 42)
    unlockedAchievement?.let { achievement ->
        AchievementUnlockDialog(
            achievement = achievement,
            onDismiss = { unlockedAchievement = null }
        )
    }
    }
}
