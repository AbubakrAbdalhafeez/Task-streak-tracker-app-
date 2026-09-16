package com.abubakr.taskstreak.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abubakr.taskstreak.data.db.AppDatabase
import com.abubakr.taskstreak.data.drive.DriveSyncState
import com.abubakr.taskstreak.data.drive.DriveSyncWorker
import com.abubakr.taskstreak.data.drive.GoogleDriveManager
import com.abubakr.taskstreak.data.model.Achievement
import com.abubakr.taskstreak.data.model.AchievementEvaluator
import com.abubakr.taskstreak.data.model.CategoryEntity
import com.abubakr.taskstreak.data.model.CompletionLogEntity
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.data.preferences.SettingsPreferences
import com.abubakr.taskstreak.data.repository.StreakRepository
import com.abubakr.taskstreak.util.DateUtils
import com.abubakr.taskstreak.util.NotificationHelper
import com.abubakr.taskstreak.util.OverallStats
import com.abubakr.taskstreak.util.StreakCalculator
import com.abubakr.taskstreak.util.TaskStreakStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class StreakViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = StreakRepository(db.taskDao(), db.completionLogDao(), db.categoryDao(), db.subtaskDao())
    val preferences = SettingsPreferences(application)

    val tasks: StateFlow<List<TaskEntity>> = repository.allActiveTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs: StateFlow<List<CompletionLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subtasks: StateFlow<List<com.abubakr.taskstreak.data.model.SubtaskEntity>> = repository.allSubtasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subtasksMap: StateFlow<Map<Long, List<com.abubakr.taskstreak.data.model.SubtaskEntity>>> = subtasks.combine(tasks) { allSubs, _ ->
        allSubs.groupBy { it.taskId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    // Map of Task ID to set of completed dates
    val taskCompletionsMap: StateFlow<Map<Long, Set<String>>> = logs.combine(tasks) { allLogs, _ ->
        val map = mutableMapOf<Long, MutableSet<String>>()
        for (log in allLogs) {
            map.getOrPut(log.taskId) { mutableSetOf() }.add(log.date)
        }
        map
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Map of Task ID to TaskStreakStats
    val taskStatsMap: StateFlow<Map<Long, TaskStreakStats>> = combine(tasks, taskCompletionsMap) { allTasks, completionsMap ->
        val today = LocalDate.now()
        allTasks.associate { task ->
            val set = completionsMap[task.id] ?: emptySet()
            task.id to StreakCalculator.calculateTaskStats(task, set, today)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    // Overall stats
    val overallStats: StateFlow<OverallStats> = combine(tasks, taskStatsMap) { allTasks, statsMap ->
        StreakCalculator.calculateOverallStats(allTasks, statsMap)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        OverallStats(0, 0, 0, 0, 0f, 0, emptyList())
    )

    // Achievements
    val achievements: StateFlow<List<Achievement>> = combine(overallStats, taskStatsMap) { stats, statsMap ->
        AchievementEvaluator.evaluateAchievements(stats, statsMap)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun toggleTaskToday(taskId: Long) {
        val todayStr = DateUtils.todayString()
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleTaskCompletion(taskId, todayStr)
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun toggleTaskOnDate(taskId: Long, date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleTaskCompletion(taskId, date)
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun saveTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            if (task.id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
            }
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun saveTaskWithSubtasks(task: TaskEntity, subtaskTitles: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            val taskId = if (task.id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
                task.id
            }
            // Update subtasks
            if (subtaskTitles.isNotEmpty()) {
                val existing = repository.getSubtasksForTaskDirect(taskId)
                val existingMap = existing.associateBy { it.title }
                val newSubtasks = subtaskTitles.mapIndexed { index, title ->
                    com.abubakr.taskstreak.data.model.SubtaskEntity(
                        id = existingMap[title]?.id ?: 0L,
                        taskId = taskId,
                        title = title,
                        isCompleted = existingMap[title]?.isCompleted ?: false,
                        orderIndex = index
                    )
                }
                for (sub in newSubtasks) {
                    if (sub.id == 0L) repository.insertSubtask(sub) else repository.updateSubtask(sub)
                }
                // Delete removed subtasks
                val newTitlesSet = subtaskTitles.toSet()
                for (oldSub in existing) {
                    if (!newTitlesSet.contains(oldSub.title)) {
                        repository.deleteSubtask(oldSub)
                    }
                }
            }
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun toggleSubtask(subtask: com.abubakr.taskstreak.data.model.SubtaskEntity, isCompleted: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.setSubtaskCompleted(subtask.id, isCompleted)
            val task = tasks.value.find { it.id == subtask.taskId }
            if (task != null && task.autoCompleteWithSubtasks) {
                val allTaskSubs = repository.getSubtasksForTaskDirect(task.id)
                val updatedSubs = allTaskSubs.map { if (it.id == subtask.id) it.copy(isCompleted = isCompleted) else it }
                val allDone = updatedSubs.isNotEmpty() && updatedSubs.all { it.isCompleted }
                val todayStr = DateUtils.todayString()
                repository.setTaskCompletion(task.id, todayStr, allDone)
                com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
            }
        }
    }

    fun addSubtask(taskId: Long, title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertSubtask(
                com.abubakr.taskstreak.data.model.SubtaskEntity(
                    taskId = taskId,
                    title = title.trim(),
                    isCompleted = false
                )
            )
        }
    }

    fun deleteSubtask(subtask: com.abubakr.taskstreak.data.model.SubtaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSubtask(subtask)
        }
    }

    fun incrementPomodoro(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = tasks.value.find { it.id == taskId } ?: return@launch
            val updated = task.copy(pomodoroCount = task.pomodoroCount + 1)
            repository.updateTask(updated)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTask(task)
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun addCategory(name: String, colorHex: String, iconName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.addCategory(
                CategoryEntity(
                    name = name.trim(),
                    colorHex = colorHex,
                    iconName = iconName
                )
            )
        }
    }

    fun setDailyGoal(goal: Int) {
        preferences.setDailyGoal(goal)
    }

    fun setThemeMode(mode: String) {
        preferences.setThemeMode(mode)
    }

    fun setMorningReminderEnabled(enabled: Boolean) {
        preferences.setMorningReminderEnabled(enabled)
    }

    fun setMorningReminderTime(time: String) {
        preferences.setMorningReminderTime(time)
    }

    fun setReminderOnlyIfPending(onlyIfPending: Boolean) {
        preferences.setReminderOnlyIfPending(onlyIfPending)
    }

    fun exportData(onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = repository.exportDataAsJson()
            launch(Dispatchers.Main) {
                onResult(json)
            }
        }
    }

    fun importData(jsonString: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val res = repository.importDataFromJson(jsonString)
            launch(Dispatchers.Main) {
                if (res.isSuccess) {
                    onResult(true, "Successfully imported ${res.getOrNull()} tasks!")
                } else {
                    onResult(false, res.exceptionOrNull()?.localizedMessage ?: "Import failed")
                }
            }
        }
    }

    val driveManager = GoogleDriveManager(application)
    private val _driveSyncState = MutableStateFlow<DriveSyncState>(DriveSyncState.Idle)
    val driveSyncState: StateFlow<DriveSyncState> = _driveSyncState.asStateFlow()

    init {
        // Schedule periodic 30-minute widget updates and refresh on launch
        com.abubakr.taskstreak.widget.WidgetUpdateWorker.schedule(application)
        com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(application)

        // Schedule local weekly auto-backup if enabled
        if (preferences.autoBackupLocalEnabled.value) {
            com.abubakr.taskstreak.data.backup.LocalAutoBackupWorker.schedulePeriodicBackup(application)
        }

        // Check signed-in account on startup and restore info
        val account = driveManager.getSignedInAccount()
        if (account != null) {
            preferences.setDriveUserInfo(
                account.email,
                account.displayName,
                account.photoUrl?.toString()
            )
            if (preferences.driveAutoSync.value) {
                // Auto sync on app start
                syncToDrive()
            }
        }
    }

    fun onGoogleSignInSuccess(account: com.google.android.gms.auth.api.signin.GoogleSignInAccount) {
        preferences.setDriveUserInfo(
            account.email,
            account.displayName,
            account.photoUrl?.toString()
        )
        _driveSyncState.value = DriveSyncState.Synced("Connected as ${account.displayName ?: account.email}")
        if (preferences.driveAutoSync.value) {
            DriveSyncWorker.scheduleAutoSync(getApplication())
        }
    }

    fun signOutFromDrive() {
        viewModelScope.launch {
            _driveSyncState.value = DriveSyncState.Syncing
            driveManager.signOut()
            preferences.clearDriveUserInfo()
            DriveSyncWorker.cancelAutoSync(getApplication())
            _driveSyncState.value = DriveSyncState.Idle
        }
    }

    fun setDriveAutoSync(enabled: Boolean) {
        preferences.setDriveAutoSync(enabled)
        if (enabled) {
            DriveSyncWorker.scheduleAutoSync(getApplication())
            syncToDrive()
        } else {
            DriveSyncWorker.cancelAutoSync(getApplication())
        }
    }

    fun syncToDrive() {
        if (_driveSyncState.value is DriveSyncState.Syncing) return
        viewModelScope.launch(Dispatchers.IO) {
            _driveSyncState.value = DriveSyncState.Syncing
            val json = repository.exportDataAsJson()
            val res = driveManager.backupData(json)
            val now = System.currentTimeMillis()
            if (res.isSuccess) {
                preferences.setDriveLastSyncedTime(now)
                _driveSyncState.value = DriveSyncState.Synced(res.getOrNull() ?: "Backup uploaded", now)
            } else {
                _driveSyncState.value = DriveSyncState.Error(res.exceptionOrNull()?.localizedMessage ?: "Sync error")
            }
        }
    }

    fun restoreFromDrive() {
        if (_driveSyncState.value is DriveSyncState.Syncing) return
        viewModelScope.launch(Dispatchers.IO) {
            _driveSyncState.value = DriveSyncState.Syncing
            val res = driveManager.restoreData()
            if (res.isSuccess) {
                val json = res.getOrNull().orEmpty()
                val importRes = repository.importDataFromJson(json)
                if (importRes.isSuccess) {
                    val count = importRes.getOrNull() ?: 0
                    val now = System.currentTimeMillis()
                    preferences.setDriveLastSyncedTime(now)
                    _driveSyncState.value = DriveSyncState.Synced("Restored $count tasks from Drive!", now)
                } else {
                    _driveSyncState.value = DriveSyncState.Error(importRes.exceptionOrNull()?.localizedMessage ?: "Failed parsing Drive backup")
                }
            } else {
                _driveSyncState.value = DriveSyncState.Error(res.exceptionOrNull()?.localizedMessage ?: "Failed reading from Drive")
            }
        }
    }

    fun resetDriveSyncState() {
        _driveSyncState.value = DriveSyncState.Idle
    }

    fun setAutoBackupLocalEnabled(enabled: Boolean) {
        preferences.setAutoBackupLocalEnabled(enabled)
        if (enabled) {
            com.abubakr.taskstreak.data.backup.LocalAutoBackupWorker.schedulePeriodicBackup(getApplication())
        } else {
            com.abubakr.taskstreak.data.backup.LocalAutoBackupWorker.cancelPeriodicBackup(getApplication())
        }
    }

    fun backupToDownloadsNow(onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val json = repository.exportDataAsJson()
                val result = com.abubakr.taskstreak.data.backup.LocalAutoBackupWorker.performBackupToDownloads(
                    getApplication(),
                    json
                )
                if (result.isSuccess) {
                    val fileName = result.getOrNull() ?: "backup.json"
                    preferences.setLastLocalBackupTime(System.currentTimeMillis())
                    withContext(Dispatchers.Main) {
                        onResult(true, "Saved to Downloads: $fileName")
                    }
                } else {
                    val err = result.exceptionOrNull()?.localizedMessage ?: "Failed saving backup"
                    withContext(Dispatchers.Main) {
                        onResult(false, err)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    onResult(false, e.localizedMessage ?: "Error during backup")
                }
            }
        }
    }

    fun sendTestNotification() {
        val count = overallStats.value.totalScheduledTodayCount - overallStats.value.completedTodayCount
        val msg = if (count > 0) {
            "You have $count task(s) waiting today! Keep your streaks alive 🔥"
        } else {
            "Awesome job! All tasks are completed for today ✨"
        }
        NotificationHelper.showReminderNotification(
            getApplication(),
            "Task Streak Tracker 🔥",
            msg
        )
    }
}
