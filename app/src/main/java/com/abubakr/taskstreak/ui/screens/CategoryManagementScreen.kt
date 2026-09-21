package com.abubakr.taskstreak.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.abubakr.taskstreak.data.model.CategoryEntity
import com.abubakr.taskstreak.ui.components.EmptyStateType
import com.abubakr.taskstreak.ui.components.StreakEmptyState
import com.abubakr.taskstreak.ui.viewmodel.StreakViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManagementScreen(
    viewModel: StreakViewModel,
    onBack: () -> Unit
) {
    val categories by viewModel.categories.collectAsState()
    val tasks by viewModel.tasks.collectAsState()
    val hideUnused by viewModel.preferences.hideUnusedCategories.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var categoryToEdit by remember { mutableStateOf<CategoryEntity?>(null) }
    var categoryToDelete by remember { mutableStateOf<CategoryEntity?>(null) }

    // Count tasks per category
    val taskCountMap = remember(tasks) {
        tasks.groupingBy { it.category }.eachCount()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.categories_title), fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("category_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_category_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Category")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            // Setting: Hide Unused Categories
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Hide Unused Categories", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Only show categories with active tasks in filter bar", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = hideUnused,
                        onCheckedChange = { viewModel.preferences.setHideUnusedCategories(it) },
                        modifier = Modifier.testTag("hide_unused_switch")
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            if (categories.isEmpty()) {
                StreakEmptyState(
                    type = EmptyStateType.NO_CATEGORIES,
                    onAction = { showAddDialog = true }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(categories, key = { it.id }) { category ->
                        val count = taskCountMap[category.name] ?: 0
                        CategoryListItem(
                            category = category,
                            taskCount = count,
                            onEdit = { categoryToEdit = category },
                            onDelete = { categoryToDelete = category }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CategoryEditDialog(
            category = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, colorHex, iconName ->
                viewModel.addCategory(name, colorHex, iconName)
                showAddDialog = false
            }
        )
    }

    categoryToEdit?.let { cat ->
        CategoryEditDialog(
            category = cat,
            onDismiss = { categoryToEdit = null },
            onSave = { name, colorHex, iconName ->
                viewModel.updateCategory(cat.copy(name = name, colorHex = colorHex, iconName = iconName))
                categoryToEdit = null
            }
        )
    }

    categoryToDelete?.let { cat ->
        val count = taskCountMap[cat.name] ?: 0
        AlertDialog(
            onDismissRequest = { categoryToDelete = null },
            title = { Text("Delete Category?") },
            text = {
                Text(
                    if (count > 0)
                        "Deleting \"${cat.name}\" will reassign its $count active task(s) to 'General'."
                    else
                        "Are you sure you want to delete \"${cat.name}\"?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteCategory(cat)
                        categoryToDelete = null
                    },
                    modifier = Modifier.testTag("confirm_delete_category")
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { categoryToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CategoryListItem(
    category: CategoryEntity,
    taskCount: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val color = try {
        Color(android.graphics.Color.parseColor(category.colorHex))
    } catch (_: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().testTag("category_card_${category.name}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(color.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(color, CircleShape)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(category.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("$taskCount task${if (taskCount != 1) "s" else ""}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Row {
                IconButton(onClick = onEdit, modifier = Modifier.testTag("edit_category_${category.name}")) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.outline)
                }
                if (category.name != "General") {
                    IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_category_${category.name}")) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryEditDialog(
    category: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, colorHex: String, iconName: String) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedColor by remember { mutableStateOf(category?.colorHex ?: "#FF6B35") }
    var error by remember { mutableStateOf<String?>(null) }

    val presetColors = listOf(
        "#FF6B35", "#4CAF50", "#2196F3", "#9C27B0", "#E91E63",
        "#FF9800", "#009688", "#3F51B5", "#F44336", "#607D8B"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (category == null) "New Category" else "Edit Category") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        error = null
                    },
                    label = { Text("Category Name") },
                    isError = error != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("category_name_input")
                )
                if (error != null) {
                    Text(error!!, color = MaterialTheme.colorScheme.error, fontSize = 12.sp)
                }

                Spacer(Modifier.height(16.dp))
                Text("Select Badge Color", fontWeight = FontWeight.Medium, fontSize = 13.sp)
                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    presetColors.take(5).forEach { hex ->
                        ColorSelectCircle(hex = hex, selected = selectedColor == hex, onSelect = { selectedColor = hex })
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    presetColors.drop(5).forEach { hex ->
                        ColorSelectCircle(hex = hex, selected = selectedColor == hex, onSelect = { selectedColor = hex })
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.trim().isBlank()) {
                        error = "Name cannot be empty"
                        return@TextButton
                    }
                    onSave(name.trim(), selectedColor, category?.iconName ?: "Bookmark")
                },
                modifier = Modifier.testTag("save_category_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ColorSelectCircle(
    hex: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val color = try { Color(android.graphics.Color.parseColor(hex)) } catch (_: Exception) { Color.Gray }
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(color, CircleShape)
            .clickable(onClick = onSelect),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }
    }
}
