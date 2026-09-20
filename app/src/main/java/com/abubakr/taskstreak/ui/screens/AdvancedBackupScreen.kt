package com.abubakr.taskstreak.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.ViewHeadline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.abubakr.taskstreak.ui.viewmodel.StreakViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupFileInfo(
    val name: String,
    val sizeBytes: Long,
    val lastModified: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedBackupScreen(
    viewModel: StreakViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val backupList = remember { mutableStateListOf<BackupFileInfo>() }
    var mergeMode by remember { mutableStateOf(true) }
    var pendingJsonContent by remember { mutableStateOf<String?>(null) }
    var exportPreviewContent by remember { mutableStateOf<Pair<String, String>?>(null) } // title to content

    fun refreshBackups() {
        backupList.clear()
        try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (downloadsDir.exists() && downloadsDir.isDirectory) {
                val files = downloadsDir.listFiles { file ->
                    file.isFile && file.name.startsWith("task-streak-backup-") && file.name.endsWith(".json")
                } ?: emptyArray()
                files.sortByDescending { it.lastModified() }
                backupList.addAll(files.map { BackupFileInfo(it.name, it.length(), it.lastModified()) })
            }
        } catch (_: Exception) {}
    }

    LaunchedEffect(Unit) {
        refreshBackups()
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val text = stream.bufferedReader().readText()
                    pendingJsonContent = text
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error reading file: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Advanced Backup & Desktop Sync", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("backup_back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Import / Restore Options
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Import & Restore Data", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Merge with Existing Data", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(
                                    if (mergeMode) "Keep current tasks & add imported ones" else "Replace/overwrite matching tasks",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = mergeMode,
                                onCheckedChange = { mergeMode = it },
                                modifier = Modifier.testTag("merge_mode_switch")
                            )
                        }

                        Spacer(Modifier.height(12.dp))
                        Button(
                            onClick = { filePickerLauncher.launch("application/json") },
                            modifier = Modifier.fillMaxWidth().testTag("select_file_import_button")
                        ) {
                            Icon(Icons.Default.Upload, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Select Backup JSON to Import")
                        }
                    }
                }
            }

            // Desktop Companion & Markdown Export
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Multi-Platform Sync & Desktop", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text(
                            "Export your tasks, chains, and logs for Windows/Mac desktop companions or Markdown notes (Obsidian, Notion)",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.exportDesktopJson { json ->
                                        exportPreviewContent = "Desktop Companion JSON" to json
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("export_desktop_button")
                            ) {
                                Icon(Icons.Default.Computer, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Desktop JSON", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    viewModel.exportMarkdown { md ->
                                        exportPreviewContent = "Obsidian / Notion Markdown" to md
                                    }
                                },
                                modifier = Modifier.weight(1f).testTag("export_markdown_button")
                            ) {
                                Icon(Icons.Default.ViewHeadline, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(6.dp))
                                Text("Markdown", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            // Version History of Local Snapshots
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Local Backup History (Downloads)", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    IconButton(onClick = { refreshBackups() }) {
                        Icon(Icons.Default.History, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (backupList.isEmpty()) {
                item {
                    Text(
                        "No automatic backup snapshots found in Downloads yet. Enable auto-backup in Settings to generate weekly snapshots.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                items(backupList) { fileInfo ->
                    BackupFileItem(
                        fileInfo = fileInfo,
                        onRestore = {
                            try {
                                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                                val target = File(downloadsDir, fileInfo.name)
                                if (target.exists()) {
                                    val text = target.readText()
                                    pendingJsonContent = text
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Cannot read file: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }
        }
    }

    // Confirmation / Preview for Import
    pendingJsonContent?.let { json ->
        AlertDialog(
            onDismissRequest = { pendingJsonContent = null },
            title = { Text("Confirm Import") },
            text = {
                Text(
                    if (mergeMode)
                        "Importing in MERGE mode: New habits and progress will be added to your current data without deleting anything."
                    else
                        "Importing in REPLACE mode: Colliding habits will be updated with the backup's data."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.importAdvancedJson(json, mergeMode = mergeMode) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            pendingJsonContent = null
                        }
                    },
                    modifier = Modifier.testTag("confirm_import_button")
                ) {
                    Text("Import Now")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingJsonContent = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Export Preview Modal
    exportPreviewContent?.let { (title, content) ->
        AlertDialog(
            onDismissRequest = { exportPreviewContent = null },
            title = { Text(title) },
            text = {
                Column {
                    Text(
                        content.take(400) + if (content.length > 400) "\n... (${content.length} characters)" else "",
                        fontSize = 11.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText(title, content))
                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                        exportPreviewContent = null
                    },
                    modifier = Modifier.testTag("copy_export_button")
                ) {
                    Text("Copy")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, content)
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Share $title"))
                        exportPreviewContent = null
                    }
                ) {
                    Text("Share")
                }
            }
        )
    }
}

@Composable
private fun BackupFileItem(
    fileInfo: BackupFileInfo,
    onRestore: () -> Unit
) {
    val dateStr = SimpleDateFormat("MMM d, yyyy HH:mm", Locale.getDefault()).format(Date(fileInfo.lastModified))
    val sizeKb = (fileInfo.sizeBytes / 1024).coerceAtLeast(1)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(fileInfo.name, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                Text("$dateStr • ${sizeKb} KB", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            TextButton(onClick = onRestore, modifier = Modifier.testTag("restore_file_${fileInfo.name}")) {
                Text("Restore")
            }
        }
    }
}
