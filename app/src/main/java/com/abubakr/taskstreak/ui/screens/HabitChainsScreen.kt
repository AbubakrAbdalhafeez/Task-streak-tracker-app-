package com.abubakr.taskstreak.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abubakr.taskstreak.R
import com.abubakr.taskstreak.data.model.HabitChainEntity
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.ui.viewmodel.StreakViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitChainsScreen(
    viewModel: StreakViewModel,
    onBack: () -> Unit
) {
    val chains by viewModel.habitChains.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val completionsMap by viewModel.taskCompletionsMap.collectAsState()
    val today = com.abubakr.taskstreak.util.DateUtils.todayString()

    var showCreateDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.habit_chains_title), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Stack habits together for +50 XP bonus", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chains_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_chain_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Habit Chain")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (chains.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            "No Habit Chains created yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Chain habits together (e.g. Wake up ➔ Hydrate ➔ Stretch).\nComplete all in sequence to earn bonus XP!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 12.dp, horizontal = 0.dp)
                ) {
                    items(chains, key = { it.id }) { chain ->
                        val chainTaskIds = remember(chain.taskIds) {
                            chain.taskIds.split(",").mapNotNull { it.trim().toLongOrNull() }
                        }
                        val chainTasks = tasks.filter { chainTaskIds.contains(it.id) }
                        val completedCount = chainTasks.count { task ->
                            completionsMap[task.id]?.contains(today) == true
                        }

                        HabitChainCard(
                            chain = chain,
                            tasks = chainTasks,
                            completedCount = completedCount,
                            today = today,
                            completionsMap = completionsMap,
                            onToggleTask = { taskId -> viewModel.toggleTaskToday(taskId) },
                            onDelete = { viewModel.deleteHabitChain(chain.id) }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateHabitChainDialog(
            availableTasks = tasks.filter { it.isHabit && !it.isArchived },
            onDismiss = { showCreateDialog = false },
            onSave = { title, desc, ids, color, bonusXp ->
                viewModel.saveHabitChain(title, desc, ids, color, bonusXp)
                showCreateDialog = false
            }
        )
    }
}

@Composable
private fun HabitChainCard(
    chain: HabitChainEntity,
    tasks: List<TaskEntity>,
    completedCount: Int,
    today: String,
    completionsMap: Map<Long, Set<String>>,
    onToggleTask: (Long) -> Unit,
    onDelete: () -> Unit
) {
    val total = tasks.size.coerceAtLeast(1)
    val progress = (completedCount.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    val isComplete = completedCount == total && total > 0

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().testTag("chain_card_${chain.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Link,
                        contentDescription = null,
                        tint = if (isComplete) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(chain.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        if (chain.description.isNotBlank()) {
                            Text(chain.description, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "+${chain.bonusXp} XP",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Chain", tint = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = if (isComplete) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(Modifier.height(6.dp))
            Text(
                if (isComplete) "🎉 Chain completed! +${chain.bonusXp} XP Earned" else "$completedCount of $total completed today",
                fontSize = 12.sp,
                fontWeight = if (isComplete) FontWeight.Bold else FontWeight.Normal,
                color = if (isComplete) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))

            // Sequence of tasks
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                tasks.forEachIndexed { index, task ->
                    val isDone = completionsMap[task.id]?.contains(today) == true
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isDone) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        IconButton(onClick = { onToggleTask(task.id) }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = if (isDone) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        }
                        Spacer(Modifier.width(6.dp))
                        Text("${index + 1}. ${task.title}", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateHabitChainDialog(
    availableTasks: List<TaskEntity>,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, ids: List<Long>, color: String, bonusXp: Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    val selectedIds = remember { mutableStateListOf<Long>() }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Habit Stack") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it; error = null },
                    label = { Text("Stack Title (e.g. Morning Protocol)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("chain_title_input")
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Short Description") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Text("Select Habits to Chain in Sequence:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))

                if (availableTasks.isEmpty()) {
                    Text("No habits available. Create habits first!", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                } else {
                    LazyColumn(modifier = Modifier.height(180.dp)) {
                        items(availableTasks) { task ->
                            val isChecked = selectedIds.contains(task.id)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedIds.add(task.id) else selectedIds.remove(task.id)
                                    }
                                )
                                Text(task.title, fontSize = 14.sp)
                            }
                        }
                    }
                }

                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isBlank()) {
                        error = "Title cannot be empty"
                        return@TextButton
                    }
                    if (selectedIds.size < 2) {
                        error = "Select at least 2 habits to build a chain"
                        return@TextButton
                    }
                    onSave(title, description, selectedIds.toList(), "#FF6B35", 50)
                },
                modifier = Modifier.testTag("save_chain_button")
            ) {
                Text("Create Chain")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
