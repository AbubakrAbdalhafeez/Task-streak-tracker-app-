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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalFireDepartment
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.ui.components.AddCategoryDialog
import com.abubakr.taskstreak.ui.components.DailyGoalCard
import com.abubakr.taskstreak.ui.components.DynamicGreetingCard
import com.abubakr.taskstreak.ui.components.TaskCard
import com.abubakr.taskstreak.ui.components.TaskEditDialog
import com.abubakr.taskstreak.ui.theme.DangerRed
import com.abubakr.taskstreak.ui.theme.FlamePrimary
import com.abubakr.taskstreak.ui.viewmodel.StreakViewModel
import com.abubakr.taskstreak.util.SoundAndHapticHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: StreakViewModel,
    onNavigateToCalendarForTask: (TaskEntity) -> Unit,
    onStartPomodoroForTask: ((TaskEntity) -> Unit)? = null,
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

    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskToEdit by remember { mutableStateOf<TaskEntity?>(null) }
    var taskToDelete by remember { mutableStateOf<TaskEntity?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }

    val filteredTasks = remember(tasks, selectedCategory, taskStatsMap) {
        val list = if (selectedCategory == null) tasks else tasks.filter { it.category == selectedCategory }
        // Sort: scheduled & not done today first, then completed today
        list.sortedWith(
            compareBy<TaskEntity> { task ->
                val stats = taskStatsMap[task.id]
                if (stats?.isCompletedToday == true) 1 else 0
            }.thenByDescending { it.createdAt }
        )
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = FlamePrimary,
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
        },
        modifier = modifier.fillMaxSize()
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
                                .background(FlamePrimary),
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
                        // "All" chip
                        FilterChip(
                            selected = selectedCategory == null,
                            onClick = { viewModel.setSelectedCategory(null) },
                            label = { Text("All (${tasks.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FlamePrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_all")
                        )

                        // Category chips
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat.name
                            val catColor = try {
                                Color(android.graphics.Color.parseColor(cat.colorHex))
                            } catch (_: Exception) { FlamePrimary }

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
                        text = if (selectedCategory == null) "All Tasks / المهام" else "$selectedCategory Tasks",
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
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No tasks yet in this category",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap the + button below to create your first streak habit!",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredTasks, key = { it.id }) { task ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        TaskCard(
                            task = task,
                            stats = taskStatsMap[task.id],
                            subtasks = subtasksMap[task.id] ?: emptyList(),
                            onToggleDoneToday = {
                                viewModel.toggleTaskToday(task.id)
                                SoundAndHapticHelper.playSuccessFeedback(context, soundEnabled, hapticsEnabled)
                            },
                            onEditTask = { taskToEdit = task },
                            onDeleteTask = { taskToDelete = task },
                            onViewCalendar = { onNavigateToCalendarForTask(task) },
                            onToggleSubtask = { sub, isDone ->
                                viewModel.toggleSubtask(sub, isDone)
                                SoundAndHapticHelper.playTickFeedback(context, soundEnabled, hapticsEnabled)
                            },
                            onStartPomodoro = {
                                onStartPomodoroForTask?.invoke(task)
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Task Dialog
    if (showAddTaskDialog || taskToEdit != null) {
        val currentSubtaskTitles = remember(taskToEdit, subtasksMap) {
            taskToEdit?.let { subtasksMap[it.id]?.map { s -> s.title } } ?: emptyList()
        }
        TaskEditDialog(
            taskToEdit = taskToEdit,
            categories = categories,
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

    // Delete Confirmation Dialog
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
            title = { Text("Delete Task? / تأكيد الحذف") },
            text = {
                Text(
                    "Are you sure you want to delete \"${taskToDelete?.title}\"? All associated streak history will be permanently deleted."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        taskToDelete?.let { viewModel.deleteTask(it) }
                        taskToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_button")
                ) {
                    Text("Delete / حذف", color = DangerRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { taskToDelete = null }) {
                    Text("Cancel / تراجع")
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
}
