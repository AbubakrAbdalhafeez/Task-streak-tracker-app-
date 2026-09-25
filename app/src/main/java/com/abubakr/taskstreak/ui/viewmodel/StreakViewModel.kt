package com.abubakr.taskstreak.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.abubakr.taskstreak.data.auth.AuthResult
import com.abubakr.taskstreak.data.auth.AuthUserState
import com.abubakr.taskstreak.data.auth.FirebaseAuthManager
import com.abubakr.taskstreak.data.db.AppDatabase
import com.abubakr.taskstreak.data.drive.DriveSyncState
import com.abubakr.taskstreak.data.drive.DriveSyncWorker
import com.abubakr.taskstreak.data.drive.GoogleDriveManager
import com.abubakr.taskstreak.data.model.Achievement
import com.abubakr.taskstreak.data.model.AchievementEvaluator
import com.abubakr.taskstreak.data.model.CategoryEntity
import com.abubakr.taskstreak.data.model.CompletionLogEntity
import com.abubakr.taskstreak.data.model.GamificationCalculator
import com.abubakr.taskstreak.data.model.HabitChainEntity
import com.abubakr.taskstreak.data.model.Quest
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.data.model.UserLevel
import com.abubakr.taskstreak.data.preferences.SettingsPreferences
import com.abubakr.taskstreak.data.repository.StreakRepository
import com.abubakr.taskstreak.util.CalendarSyncHelper
import com.abubakr.taskstreak.util.DataInsightsEngine
import com.abubakr.taskstreak.util.DateUtils
import com.abubakr.taskstreak.util.MotivationEngine
import com.abubakr.taskstreak.util.NetworkMonitor
import com.abubakr.taskstreak.util.NotificationHelper
import com.abubakr.taskstreak.util.OverallStats
import com.abubakr.taskstreak.util.ParsedVoiceTask
import com.abubakr.taskstreak.util.ProductivityInsight
import com.abubakr.taskstreak.util.SocialShareHelper
import com.abubakr.taskstreak.util.StreakCalculator
import com.abubakr.taskstreak.util.TaskStreakStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import com.abubakr.taskstreak.data.model.FocusSessionMode
import com.abubakr.taskstreak.data.model.FocusAutoStartType
import com.abubakr.taskstreak.data.model.FocusSessionStatus
import com.abubakr.taskstreak.data.model.FocusRecord
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class StreakViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = StreakRepository(
        db.taskDao(),
        db.completionLogDao(),
        db.categoryDao(),
        db.subtaskDao(),
        db.habitChainDao()
    )
    val preferences = SettingsPreferences(application)
    val networkMonitor = NetworkMonitor(application)
    val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    val tasks: StateFlow<List<TaskEntity>> = repository.allActiveTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val archivedTasks: StateFlow<List<TaskEntity>> = repository.archivedTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val habitChains: StateFlow<List<HabitChainEntity>> = repository.allChains
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val logs: StateFlow<List<CompletionLogEntity>> = repository.allLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val categories: StateFlow<List<CategoryEntity>> = repository.allCategories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subtasks: StateFlow<List<com.abubakr.taskstreak.data.model.SubtaskEntity>> = repository.allSubtasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Gamification state
    val totalXp: StateFlow<Long> = preferences.totalXp
    val streakShields: StateFlow<Int> = preferences.streakShields
    val streakShieldsUsed: StateFlow<Int> = preferences.streakShieldsUsed

    val userLevel: StateFlow<UserLevel> = totalXp.map {
        GamificationCalculator.calculateLevel(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GamificationCalculator.calculateLevel(120L))

    // Data Insights
    val productivityInsights: StateFlow<ProductivityInsight> = logs.map {
        DataInsightsEngine.generateInsights(it)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DataInsightsEngine.generateInsights(emptyList()))

    val todayQuote = MotivationEngine.getTodayQuote()

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

    // Search, Filter, Sort
    val searchQuery = MutableStateFlow("")
    val statusFilter = MutableStateFlow("ALL") // ALL, PENDING, COMPLETED
    val typeFilter = MutableStateFlow("ALL")     // ALL, HABIT, TASK
    val sortOption = MutableStateFlow("DEFAULT") // DEFAULT, STREAK, NAME, RECENT
    val selectedTimeframe = MutableStateFlow(com.abubakr.taskstreak.util.TimeframeFilter.TODAY)
    val taskInternalFilter = MutableStateFlow(com.abubakr.taskstreak.util.TaskInternalFilter.ALL_STATUS)

    fun setTimeframe(filter: com.abubakr.taskstreak.util.TimeframeFilter) {
        selectedTimeframe.value = filter
    }

    fun setInternalFilter(filter: com.abubakr.taskstreak.util.TaskInternalFilter) {
        taskInternalFilter.value = filter
    }

    // Bulk selection state
    val isSelectionMode = MutableStateFlow(false)
    val selectedTaskIds = MutableStateFlow<Set<Long>>(emptySet())

    // Undo state
    private var lastDeletedTask: TaskEntity? = null
    private var lastDeletedDates: Set<String> = emptySet()
    private var lastToggledTaskId: Long? = null
    private var lastToggledDate: String? = null
    private var lastToggledWasComplete: Boolean = false

    fun toggleSelection(taskId: Long) {
        val current = selectedTaskIds.value.toMutableSet()
        if (current.contains(taskId)) {
            current.remove(taskId)
        } else {
            current.add(taskId)
        }
        selectedTaskIds.value = current
        if (current.isEmpty()) {
            isSelectionMode.value = false
        }
    }

    fun startSelection(taskId: Long) {
        isSelectionMode.value = true
        selectedTaskIds.value = setOf(taskId)
    }

    fun selectAll(allIds: List<Long>) {
        selectedTaskIds.value = allIds.toSet()
    }

    fun clearSelection() {
        isSelectionMode.value = false
        selectedTaskIds.value = emptySet()
    }

    fun bulkCompleteSelected() {
        val ids = selectedTaskIds.value.toList()
        if (ids.isEmpty()) return
        val todayStr = DateUtils.todayString()
        viewModelScope.launch(Dispatchers.IO) {
            repository.bulkSetCompletion(ids, todayStr, true)
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
            withContext(Dispatchers.Main) {
                clearSelection()
            }
        }
    }

    fun bulkDeleteSelected() {
        val ids = selectedTaskIds.value.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch(Dispatchers.IO) {
            repository.bulkDeleteTasks(ids)
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
            withContext(Dispatchers.Main) {
                clearSelection()
            }
        }
    }

    fun deleteTaskWithUndo(task: TaskEntity, onUndoAvailable: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val dates = taskCompletionsMap.value[task.id] ?: emptySet()
            lastDeletedTask = task
            lastDeletedDates = dates
            repository.deleteTask(task)
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
            withContext(Dispatchers.Main) {
                onUndoAvailable(task.title)
            }
        }
    }

    fun undoDelete() {
        val task = lastDeletedTask ?: return
        val dates = lastDeletedDates
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreTaskWithLogs(task, dates)
            lastDeletedTask = null
            lastDeletedDates = emptySet()
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun clearAllAppData(onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllDatabaseData()
            preferences.resetAllGamificationAndHistory()
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun resetDataToDefaults(onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAllDatabaseData()
            preferences.resetAllGamificationAndHistory()

            // Seed default tasks and completion logs
            val today = LocalDate.now()
            val twoWeeksAgo = today.minusDays(14).toString()

            val task1 = TaskEntity(
                id = 0,
                title = "Daily Workout & Movement",
                category = "Fitness",
                categoryColorHex = "#10B981",
                recurrenceType = "DAILY",
                startDate = twoWeeksAgo,
                reminderTime = "07:30"
            )
            val task2 = TaskEntity(
                id = 0,
                title = "English Reading (15 mins)",
                category = "English",
                categoryColorHex = "#8B5CF6",
                recurrenceType = "DAILY",
                startDate = twoWeeksAgo,
                reminderTime = "19:00"
            )
            val task3 = TaskEntity(
                id = 0,
                title = "Study & Code Practice",
                category = "Study",
                categoryColorHex = "#3B82F6",
                recurrenceType = "CUSTOM",
                customDaysOfWeek = "1,2,3,4,5",
                startDate = twoWeeksAgo,
                reminderTime = "20:00"
            )

            val id1 = repository.insertTask(task1)
            val id2 = repository.insertTask(task2)
            repository.insertTask(task3)

            for (i in 1..6) {
                repository.toggleTaskCompletion(id1, today.minusDays(i.toLong()).toString())
            }
            for (i in 1..3) {
                repository.toggleTaskCompletion(id2, today.minusDays(i.toLong()).toString())
            }

            preferences.addXp(120L)

            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun getDailyQuote(): Pair<String, String> {
        val quotes = listOf(
            "We are what we repeatedly do. Excellence, then, is not an act, but a habit." to "Aristotle",
            "Small daily improvements over time lead to stunning results." to "Robin Sharma",
            "Consistency is what transforms average into excellence." to "Tony Robbins",
            "You do not rise to the level of your goals. You fall to the level of your systems." to "James Clear",
            "Success is the sum of small efforts, repeated day in and day out." to "Robert Collier",
            "Motivation is what gets you started. Habit is what keeps you going." to "Jim Ryun",
            "Habits are the compound interest of self-improvement." to "James Clear"
        )
        val dayIndex = (LocalDate.now().dayOfYear) % quotes.size
        return quotes[dayIndex]
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }

    fun toggleTaskToday(taskId: Long) {
        val todayStr = DateUtils.todayString()
        val currentlyDone = taskCompletionsMap.value[taskId]?.contains(todayStr) == true
        lastToggledTaskId = taskId
        lastToggledDate = todayStr
        lastToggledWasComplete = !currentlyDone
        viewModelScope.launch(Dispatchers.IO) {
            repository.toggleTaskCompletion(taskId, todayStr)
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun undoToggleTaskCompletion() {
        val taskId = lastToggledTaskId ?: return
        val date = lastToggledDate ?: return
        viewModelScope.launch(Dispatchers.IO) {
            // Toggling again restores the previous state
            repository.toggleTaskCompletion(taskId, date)
            lastToggledTaskId = null
            lastToggledDate = null
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
            val taskId = if (task.id == 0L) {
                repository.insertTask(task)
            } else {
                repository.updateTask(task)
                task.id
            }
            val finalTask = task.copy(id = taskId)
            if (finalTask.reminderTime.isNullOrBlank() || finalTask.isArchived) {
                NotificationHelper.cancelTaskReminder(getApplication(), taskId)
            } else {
                NotificationHelper.scheduleTaskReminder(getApplication(), finalTask)
            }
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun duplicateTask(task: TaskEntity, onComplete: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.IO) {
            val copy = task.copy(
                id = 0,
                title = "${task.title} (Copy)",
                createdAt = System.currentTimeMillis()
            )
            val newTaskId = repository.insertTask(copy)
            val existingSubs = subtasksMap.value[task.id] ?: emptyList()
            for (sub in existingSubs) {
                repository.insertSubtask(
                    sub.copy(
                        id = 0,
                        taskId = newTaskId,
                        isCompleted = false
                    )
                )
            }
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
            withContext(Dispatchers.Main) {
                onComplete()
            }
        }
    }

    fun updateTaskSchedule(task: TaskEntity, newStartDate: String, recurrenceType: String, customDays: String) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTask(
                task.copy(
                    startDate = newStartDate,
                    recurrenceType = recurrenceType,
                    customDaysOfWeek = customDays
                )
            )
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun updateTaskReminder(task: TaskEntity, reminderTime: String?) {
        viewModelScope.launch(Dispatchers.IO) {
            val updated = task.copy(reminderTime = reminderTime)
            repository.updateTask(updated)
            if (reminderTime.isNullOrBlank() || updated.isArchived) {
                NotificationHelper.cancelTaskReminder(getApplication(), task.id)
            } else {
                NotificationHelper.scheduleTaskReminder(getApplication(), updated)
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
            val finalTask = task.copy(id = taskId)
            if (finalTask.reminderTime.isNullOrBlank() || finalTask.isArchived) {
                NotificationHelper.cancelTaskReminder(getApplication(), taskId)
            } else {
                NotificationHelper.scheduleTaskReminder(getApplication(), finalTask)
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
            NotificationHelper.cancelTaskReminder(getApplication(), task.id)
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
        NotificationHelper.scheduleMorningOverviewReminder(
            getApplication(),
            preferences.morningReminderTime.value,
            enabled
        )
    }

    fun setMorningReminderTime(time: String) {
        preferences.setMorningReminderTime(time)
        NotificationHelper.scheduleMorningOverviewReminder(
            getApplication(),
            time,
            preferences.morningReminderEnabled.value
        )
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
    val authManager = FirebaseAuthManager(application)

    val currentAuthUser: StateFlow<AuthUserState?> = authManager.currentUserFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), authManager.getCurrentUser())

    private val _driveSyncState = MutableStateFlow<DriveSyncState>(DriveSyncState.Idle)
    val driveSyncState: StateFlow<DriveSyncState> = _driveSyncState.asStateFlow()

    private val _authActionLoading = MutableStateFlow(false)
    val authActionLoading: StateFlow<Boolean> = _authActionLoading.asStateFlow()

    private val _authErrorMessage = MutableStateFlow<String?>(null)
    val authErrorMessage: StateFlow<String?> = _authErrorMessage.asStateFlow()

    fun clearAuthErrorMessage() {
        _authErrorMessage.value = null
    }

    fun signInWithEmail(email: String, pass: String, onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch {
            _authActionLoading.value = true
            _authErrorMessage.value = null
            when (val res = authManager.signInWithEmail(email, pass)) {
                is AuthResult.Success -> {
                    _authActionLoading.value = false
                    preferences.setDriveUserInfo(res.user.email, res.user.displayName, res.user.photoUrl)
                    onSuccess(res.message)
                }
                is AuthResult.Error -> {
                    _authActionLoading.value = false
                    _authErrorMessage.value = res.message
                }
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String, displayName: String, onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch {
            _authActionLoading.value = true
            _authErrorMessage.value = null
            when (val res = authManager.signUpWithEmail(email, pass, displayName)) {
                is AuthResult.Success -> {
                    _authActionLoading.value = false
                    preferences.setDriveUserInfo(res.user.email, displayName.ifBlank { res.user.displayName }, res.user.photoUrl)
                    onSuccess(res.message)
                }
                is AuthResult.Error -> {
                    _authActionLoading.value = false
                    _authErrorMessage.value = res.message
                }
            }
        }
    }

    fun signInAnonymously(onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch {
            _authActionLoading.value = true
            _authErrorMessage.value = null
            when (val res = authManager.signInAnonymously()) {
                is AuthResult.Success -> {
                    _authActionLoading.value = false
                    onSuccess(res.message)
                }
                is AuthResult.Error -> {
                    _authActionLoading.value = false
                    _authErrorMessage.value = res.message
                }
            }
        }
    }

    fun sendPasswordReset(email: String, onSuccess: (String) -> Unit = {}) {
        viewModelScope.launch {
            _authActionLoading.value = true
            _authErrorMessage.value = null
            when (val res = authManager.sendPasswordResetEmail(email)) {
                is AuthResult.Success -> {
                    _authActionLoading.value = false
                    onSuccess(res.message)
                }
                is AuthResult.Error -> {
                    _authActionLoading.value = false
                    _authErrorMessage.value = res.message
                }
            }
        }
    }

    fun signOutFromAuth() {
        authManager.signOut()
    }

    init {
        // Schedule periodic 30-minute widget updates and refresh on launch
        com.abubakr.taskstreak.widget.WidgetUpdateWorker.schedule(application)
        com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(application)

        // Reschedule all active task reminders
        NotificationHelper.rescheduleAllReminders(application)

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
        viewModelScope.launch {
            authManager.signInWithGoogleAccount(account)
        }
        _driveSyncState.value = DriveSyncState.Synced("Connected as ${account.displayName ?: account.email}")
        if (preferences.driveAutoSync.value) {
            DriveSyncWorker.scheduleAutoSync(getApplication())
        }
    }

    fun signOutFromDrive() {
        viewModelScope.launch {
            _driveSyncState.value = DriveSyncState.Syncing
            driveManager.signOut()
            authManager.signOut()
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

    fun setQuietHoursEnabled(enabled: Boolean) {
        preferences.setQuietHoursEnabled(enabled)
    }

    fun setQuietHoursRange(start: String, end: String) {
        preferences.setQuietHoursTimes(start, end)
    }

    // Feature 22: Task Archive
    fun archiveTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.archiveTask(taskId)
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun unarchiveTask(taskId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.unarchiveTask(taskId)
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    fun deleteArchivedTasks() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteArchivedTasks()
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    // Feature 23: Category Management
    fun updateCategory(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateCategory(category)
        }
    }

    fun deleteCategory(category: CategoryEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteCategory(category)
            if (_selectedCategory.value == category.name) {
                _selectedCategory.value = null
            }
        }
    }

    // Feature 31: Habit Chains
    fun saveHabitChain(title: String, description: String, taskIds: List<Long>, colorHex: String, bonusXp: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val taskIdsStr = taskIds.joinToString(",")
            val chain = HabitChainEntity(
                title = title.trim(),
                description = description.trim(),
                taskIds = taskIdsStr,
                colorHex = colorHex,
                bonusXp = bonusXp
            )
            repository.insertChain(chain)
            addXp(bonusXp.toLong())
        }
    }

    fun deleteHabitChain(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteChain(id)
        }
    }

    // Feature 34: Gamification & Quests
    fun addXp(amount: Long) {
        preferences.addXp(amount)
    }

    fun useStreakShield(): Boolean {
        return preferences.useStreakShield()
    }

    fun addStreakShield(count: Int = 1) {
        preferences.addStreakShield(count)
    }

    // Feature 35: Voice Input
    fun addVoiceTask(parsed: ParsedVoiceTask) {
        viewModelScope.launch(Dispatchers.IO) {
            val task = TaskEntity(
                title = parsed.title,
                category = parsed.category,
                recurrenceType = parsed.recurrenceType.name,
                reminderTime = parsed.reminderTime,
                startDate = DateUtils.todayString(),
                isHabit = true
            )
            repository.insertTask(task)
            com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
        }
    }

    // Feature 38: Calendar Integration
    fun syncTaskToCalendar(context: android.content.Context, task: TaskEntity) {
        CalendarSyncHelper.addTaskToCalendar(context, task)
    }

    // Feature 24 & 40: Advanced Import / Export & Desktop Sync
    fun exportDesktopJson(onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val json = repository.exportDesktopJson()
            withContext(Dispatchers.Main) {
                onResult(json)
            }
        }
    }

    fun exportMarkdown(onResult: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val md = repository.exportMarkdown()
            withContext(Dispatchers.Main) {
                onResult(md)
            }
        }
    }

    fun importAdvancedJson(
        jsonString: String,
        mergeMode: Boolean = false,
        selectedTitles: Set<String>? = null,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val res = repository.importDataFromJson(jsonString, mergeMode, selectedTitles)
            withContext(Dispatchers.Main) {
                if (res.isSuccess) {
                    val count = res.getOrDefault(0)
                    com.abubakr.taskstreak.widget.WidgetUpdater.updateAllWidgets(getApplication())
                    onResult(true, "Successfully imported $count tasks!")
                } else {
                    onResult(false, res.exceptionOrNull()?.localizedMessage ?: "Import failed")
                }
            }
        }
    }

    // ==========================================
    // POMODORO & FOCUS TIMER ARCHITECTURE
    // ==========================================
    enum class FocusPhase { WORK, SHORT_BREAK, LONG_BREAK }

    private val _focusPhase = MutableStateFlow(FocusPhase.WORK)
    val focusPhase: StateFlow<FocusPhase> = _focusPhase.asStateFlow()

    private val _focusIsRunning = MutableStateFlow(false)
    val focusIsRunning: StateFlow<Boolean> = _focusIsRunning.asStateFlow()

    private val _focusTargetSeconds = MutableStateFlow(25 * 60)
    val focusTargetSeconds: StateFlow<Int> = _focusTargetSeconds.asStateFlow()

    private val _focusRemainingSeconds = MutableStateFlow(25 * 60)
    val focusRemainingSeconds: StateFlow<Int> = _focusRemainingSeconds.asStateFlow()

    private val _focusElapsedSessionSeconds = MutableStateFlow(0)
    val focusElapsedSessionSeconds: StateFlow<Int> = _focusElapsedSessionSeconds.asStateFlow()

    private val _focusCompletedCycles = MutableStateFlow(0)
    val focusCompletedCycles: StateFlow<Int> = _focusCompletedCycles.asStateFlow()

    private val _focusSelectedTaskId = MutableStateFlow<Long?>(null)
    val focusSelectedTaskId: StateFlow<Long?> = _focusSelectedTaskId.asStateFlow()

    // 10-second transition countdown (for auto-start)
    private val _focusAutoStartCountdown = MutableStateFlow<Int?>(null) // null = not counting down
    val focusAutoStartCountdown: StateFlow<Int?> = _focusAutoStartCountdown.asStateFlow()

    private var focusTimerJob: Job? = null
    private var countdownJob: Job? = null
    private var oneMinuteWarningFired = false

    init {
        // Initialize timer with persisted duration
        val initialMins = preferences.focusLastDurationMinutes.value
        _focusTargetSeconds.value = initialMins * 60
        _focusRemainingSeconds.value = initialMins * 60
    }

    fun setFocusSelectedTask(taskId: Long?) {
        _focusSelectedTaskId.value = taskId
    }

    fun setFocusSessionMode(mode: FocusSessionMode) {
        preferences.setFocusSessionMode(mode.name)
        when (mode) {
            FocusSessionMode.CLASSIC -> {
                setFocusCustomDuration(25)
            }
            FocusSessionMode.DEEP_WORK -> {
                setFocusCustomDuration(90)
            }
            FocusSessionMode.CUSTOM -> {
                val last = preferences.focusCustomWorkMinutes.value
                setFocusCustomDuration(last)
            }
        }
    }

    fun setFocusCustomDuration(minutes: Int) {
        val safeMins = minutes.coerceIn(5, 120)
        preferences.setFocusLastDurationMinutes(safeMins)
        if (_focusPhase.value == FocusPhase.WORK && !_focusIsRunning.value) {
            _focusTargetSeconds.value = safeMins * 60
            _focusRemainingSeconds.value = safeMins * 60
            _focusElapsedSessionSeconds.value = 0
        }
    }

    fun startFocusTimer() {
        cancelCountdown()
        if (_focusIsRunning.value) return
        _focusIsRunning.value = true
        focusTimerJob = viewModelScope.launch {
            while (_focusRemainingSeconds.value > 0 && _focusIsRunning.value) {
                delay(1000L)
                if (!_focusIsRunning.value) break
                _focusRemainingSeconds.value = (_focusRemainingSeconds.value - 1).coerceAtLeast(0)
                _focusElapsedSessionSeconds.value += 1

                // 1-minute smart warning
                if (_focusRemainingSeconds.value == 60 && !oneMinuteWarningFired) {
                    oneMinuteWarningFired = true
                    if (preferences.focusOneMinuteWarningEnabled.value) {
                        NotificationHelper.showFocusNotification(
                            getApplication(),
                            "1 Minute Left!",
                            "Finish up your current thought or step. Almost there!",
                            notificationId = 3001
                        )
                    }
                }

                if (_focusRemainingSeconds.value == 0) {
                    onFocusPhaseCompleted()
                    break
                }
            }
        }
    }

    fun pauseFocusTimer() {
        _focusIsRunning.value = false
        focusTimerJob?.cancel()
        focusTimerJob = null
    }

    fun stopAndLogFocus(abandoned: Boolean = false) {
        pauseFocusTimer()
        cancelCountdown()
        val elapsedMins = _focusElapsedSessionSeconds.value / 60
        val targetMins = _focusTargetSeconds.value / 60
        val selectedTask = tasks.value.find { it.id == _focusSelectedTaskId.value }

        if (_focusPhase.value == FocusPhase.WORK && elapsedMins > 0) {
            val status = when {
                abandoned && preferences.partialCreditEnabled.value -> FocusSessionStatus.PARTIAL
                abandoned -> FocusSessionStatus.FAILED
                else -> FocusSessionStatus.COMPLETED
            }
            preferences.addFocusSessionRecord(
                taskId = selectedTask?.id,
                taskTitle = selectedTask?.title,
                targetMinutes = targetMins,
                elapsedMinutes = elapsedMins,
                status = status.name
            )

            if (status == FocusSessionStatus.COMPLETED && selectedTask != null) {
                incrementPomodoro(selectedTask.id)
            }
        }

        // Reset to fresh work session
        resetFocusTimer()
    }

    fun resetFocusTimer() {
        pauseFocusTimer()
        cancelCountdown()
        _focusPhase.value = FocusPhase.WORK
        val durationMins = preferences.focusLastDurationMinutes.value
        _focusTargetSeconds.value = durationMins * 60
        _focusRemainingSeconds.value = durationMins * 60
        _focusElapsedSessionSeconds.value = 0
        oneMinuteWarningFired = false
    }

    fun skipCurrentPhase() {
        pauseFocusTimer()
        cancelCountdown()
        transitionToNextPhase(autoTrigger = false)
    }

    fun cancelCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _focusAutoStartCountdown.value = null
    }

    private fun onFocusPhaseCompleted() {
        _focusIsRunning.value = false
        focusTimerJob?.cancel()
        focusTimerJob = null
        oneMinuteWarningFired = false

        val currentPhase = _focusPhase.value
        val selectedTask = tasks.value.find { it.id == _focusSelectedTaskId.value }
        val targetMins = _focusTargetSeconds.value / 60

        if (currentPhase == FocusPhase.WORK) {
            // Record completed session
            preferences.addFocusSessionRecord(
                taskId = selectedTask?.id,
                taskTitle = selectedTask?.title,
                targetMinutes = targetMins,
                elapsedMinutes = targetMins,
                status = FocusSessionStatus.COMPLETED.name
            )
            _focusCompletedCycles.value += 1
            if (selectedTask != null) {
                incrementPomodoro(selectedTask.id)
            }

            if (preferences.focusCompletionNotificationEnabled.value) {
                NotificationHelper.showFocusNotification(
                    getApplication(),
                    "Focus Session Complete! 🎉",
                    "Great job! You achieved ${targetMins}m of pure focus. Time for a well-deserved break.",
                    notificationId = 3002
                )
            }
        } else {
            if (preferences.focusCompletionNotificationEnabled.value) {
                NotificationHelper.showFocusNotification(
                    getApplication(),
                    "Break Over! ⚡",
                    "Ready to dive back into deep focus?",
                    notificationId = 3003
                )
            }
        }

        transitionToNextPhase(autoTrigger = true)
    }

    private fun transitionToNextPhase(autoTrigger: Boolean) {
        val currentPhase = _focusPhase.value
        val currentMode = runCatching { FocusSessionMode.valueOf(preferences.focusSessionMode.value) }
            .getOrDefault(FocusSessionMode.CLASSIC)
        val autoStart = runCatching { FocusAutoStartType.valueOf(preferences.focusAutoStartType.value) }
            .getOrDefault(FocusAutoStartType.MANUAL)

        if (autoStart == FocusAutoStartType.CONTINUOUS_NO_BREAK) {
            // Continuous: Stay in WORK, reset seconds
            _focusPhase.value = FocusPhase.WORK
            val mins = preferences.focusLastDurationMinutes.value
            _focusTargetSeconds.value = mins * 60
            _focusRemainingSeconds.value = mins * 60
            _focusElapsedSessionSeconds.value = 0
            if (autoTrigger) {
                startCountdownThenRun()
            }
            return
        }

        if (currentPhase == FocusPhase.WORK) {
            val cycles = _focusCompletedCycles.value
            val isLongBreak = when (currentMode) {
                FocusSessionMode.CLASSIC -> cycles > 0 && cycles % 4 == 0
                FocusSessionMode.DEEP_WORK -> cycles > 0 && cycles % 2 == 0
                FocusSessionMode.CUSTOM -> {
                    val req = preferences.focusCustomCyclesBeforeLongBreak.value
                    cycles > 0 && cycles % req == 0
                }
            }

            val breakMins = when {
                isLongBreak && currentMode == FocusSessionMode.CLASSIC -> 15
                isLongBreak && currentMode == FocusSessionMode.DEEP_WORK -> 20
                isLongBreak && currentMode == FocusSessionMode.CUSTOM -> preferences.focusCustomLongBreakMinutes.value
                currentMode == FocusSessionMode.DEEP_WORK -> 15
                currentMode == FocusSessionMode.CUSTOM -> preferences.focusCustomShortBreakMinutes.value
                else -> 5 // Classic short break
            }

            _focusPhase.value = if (isLongBreak) FocusPhase.LONG_BREAK else FocusPhase.SHORT_BREAK
            _focusTargetSeconds.value = breakMins * 60
            _focusRemainingSeconds.value = breakMins * 60
            _focusElapsedSessionSeconds.value = 0

            val shouldAuto = autoStart == FocusAutoStartType.BREAK_ONLY || autoStart == FocusAutoStartType.FULL_AUTO
            if (autoTrigger && shouldAuto) {
                startCountdownThenRun()
            }
        } else {
            // Break finished -> back to WORK
            _focusPhase.value = FocusPhase.WORK
            val workMins = when (currentMode) {
                FocusSessionMode.CLASSIC -> 25
                FocusSessionMode.DEEP_WORK -> 90
                FocusSessionMode.CUSTOM -> preferences.focusCustomWorkMinutes.value
            }
            _focusTargetSeconds.value = workMins * 60
            _focusRemainingSeconds.value = workMins * 60
            _focusElapsedSessionSeconds.value = 0

            val shouldAuto = autoStart == FocusAutoStartType.FULL_AUTO
            if (autoTrigger && shouldAuto) {
                startCountdownThenRun()
            }
        }
    }

    private fun startCountdownThenRun() {
        cancelCountdown()
        countdownJob = viewModelScope.launch {
            for (i in 10 downTo 1) {
                _focusAutoStartCountdown.value = i
                delay(1000L)
            }
            _focusAutoStartCountdown.value = null
            startFocusTimer()
        }
    }

    // Live daily statistics derived from records
    data class FocusDailyStats(
        val totalMinutesToday: Int,
        val completedSessionsToday: Int,
        val partialSessionsToday: Int,
        val abandonedSessionsToday: Int,
        val currentStreakDays: Int
    )

    val focusDailyStats: StateFlow<FocusDailyStats> = preferences.focusHistoryJson.map { jsonStr ->
        val todayStr = java.time.LocalDate.now().toString()
        var totalMins = 0
        var completed = 0
        var partial = 0
        var abandoned = 0
        val activeDates = mutableSetOf<String>()

        try {
            val arr = org.json.JSONArray(jsonStr)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val date = obj.optString("date", "")
                val status = obj.optString("status", "")
                val elapsed = obj.optInt("elapsedMinutes", 0)

                if (status == FocusSessionStatus.COMPLETED.name || status == FocusSessionStatus.PARTIAL.name) {
                    if (date.isNotBlank()) activeDates.add(date)
                }

                if (date == todayStr) {
                    totalMins += elapsed
                    when (status) {
                        FocusSessionStatus.COMPLETED.name -> completed++
                        FocusSessionStatus.PARTIAL.name -> partial++
                        FocusSessionStatus.FAILED.name -> abandoned++
                    }
                }
            }
        } catch (_: Exception) {}

        // Calculate consecutive focus days streak
        var streak = 0
        var checkDate = java.time.LocalDate.now()
        while (activeDates.contains(checkDate.toString())) {
            streak++
            checkDate = checkDate.minusDays(1)
        }

        FocusDailyStats(
            totalMinutesToday = totalMins,
            completedSessionsToday = completed,
            partialSessionsToday = partial,
            abandonedSessionsToday = abandoned,
            currentStreakDays = streak
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        FocusDailyStats(0, 0, 0, 0, 0)
    )
}
