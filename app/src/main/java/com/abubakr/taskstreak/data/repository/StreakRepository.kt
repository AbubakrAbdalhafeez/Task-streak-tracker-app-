package com.abubakr.taskstreak.data.repository

import com.abubakr.taskstreak.data.db.CategoryDao
import com.abubakr.taskstreak.data.db.CompletionLogDao
import com.abubakr.taskstreak.data.db.HabitChainDao
import com.abubakr.taskstreak.data.db.SubtaskDao
import com.abubakr.taskstreak.data.db.TaskDao
import com.abubakr.taskstreak.data.model.CategoryEntity
import com.abubakr.taskstreak.data.model.CompletionLogEntity
import com.abubakr.taskstreak.data.model.HabitChainEntity
import com.abubakr.taskstreak.data.model.SubtaskEntity
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.json.JSONArray
import org.json.JSONObject

class StreakRepository(
    private val taskDao: TaskDao,
    private val logDao: CompletionLogDao,
    private val categoryDao: CategoryDao,
    private val subtaskDao: SubtaskDao,
    private val habitChainDao: HabitChainDao? = null
) {
    val allActiveTasks: Flow<List<TaskEntity>> = taskDao.getAllActiveTasks()
    val archivedTasks: Flow<List<TaskEntity>> = taskDao.getArchivedTasks()
    val allLogs: Flow<List<CompletionLogEntity>> = logDao.getAllLogs()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allSubtasks: Flow<List<SubtaskEntity>> = subtaskDao.getAllSubtasks()
    val allChains: Flow<List<HabitChainEntity>> = habitChainDao?.getAllChains() ?: flowOf(emptyList())

    suspend fun archiveTask(id: Long) = taskDao.archiveTask(id)
    suspend fun unarchiveTask(id: Long) = taskDao.unarchiveTask(id)
    suspend fun deleteArchivedTasks() = taskDao.deleteArchivedTasks()

    suspend fun insertChain(chain: HabitChainEntity): Long = habitChainDao?.insert(chain) ?: 0L
    suspend fun updateChain(chain: HabitChainEntity) { habitChainDao?.update(chain) }
    suspend fun deleteChain(id: Long) { habitChainDao?.deleteById(id) }

    fun getSubtasksForTask(taskId: Long): Flow<List<SubtaskEntity>> = subtaskDao.getSubtasksForTask(taskId)
    suspend fun getSubtasksForTaskDirect(taskId: Long): List<SubtaskEntity> = subtaskDao.getSubtasksForTaskDirect(taskId)

    suspend fun insertSubtask(subtask: SubtaskEntity): Long = subtaskDao.insert(subtask)
    suspend fun updateSubtask(subtask: SubtaskEntity) = subtaskDao.update(subtask)
    suspend fun deleteSubtask(subtask: SubtaskEntity) = subtaskDao.delete(subtask)
    suspend fun setSubtaskCompleted(subtaskId: Long, isCompleted: Boolean) = subtaskDao.setSubtaskCompleted(subtaskId, isCompleted)

    suspend fun insertTask(task: TaskEntity): Long = taskDao.insert(task)

    suspend fun updateTask(task: TaskEntity) = taskDao.update(task)

    suspend fun deleteTask(task: TaskEntity) {
        logDao.deleteForTask(task.id)
        taskDao.delete(task)
    }

    suspend fun deleteTaskId(id: Long) {
        logDao.deleteForTask(id)
        taskDao.deleteById(id)
    }

    suspend fun toggleTaskCompletion(taskId: Long, date: String): Boolean {
        val existing = logDao.getLog(taskId, date)
        return if (existing != null) {
            logDao.delete(taskId, date)
            false
        } else {
            logDao.insert(CompletionLogEntity(taskId = taskId, date = date))
            true
        }
    }

    suspend fun setTaskCompletion(taskId: Long, date: String, completed: Boolean) {
        val existing = logDao.getLog(taskId, date)
        if (completed && existing == null) {
            logDao.insert(CompletionLogEntity(taskId = taskId, date = date))
        } else if (!completed && existing != null) {
            logDao.delete(taskId, date)
        }
    }

    suspend fun bulkDeleteTasks(taskIds: List<Long>) {
        for (id in taskIds) {
            logDao.deleteForTask(id)
            taskDao.deleteById(id)
        }
    }

    suspend fun bulkSetCompletion(taskIds: List<Long>, date: String, completed: Boolean) {
        for (id in taskIds) {
            setTaskCompletion(id, date, completed)
        }
    }

    suspend fun restoreTaskWithLogs(task: TaskEntity, dates: Set<String>) {
        val newId = taskDao.insert(task.copy(id = 0))
        for (d in dates) {
            logDao.insert(CompletionLogEntity(taskId = newId, date = d))
        }
    }

    suspend fun addCategory(category: CategoryEntity): Long = categoryDao.insert(category)
    suspend fun updateCategory(category: CategoryEntity) = categoryDao.update(category)
    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.delete(category)
    suspend fun deleteCategoryByName(name: String) = categoryDao.deleteByName(name)

    suspend fun exportDataAsJson(): String {
        val tasks = taskDao.getAllTasksDirect()
        val logs = logDao.getAllLogsDirect()
        val categories = categoryDao.getAllCategoriesDirect()

        val root = JSONObject()
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val tasksArray = JSONArray()
        for (task in tasks) {
            val tObj = JSONObject()
            tObj.put("id", task.id)
            tObj.put("title", task.title)
            tObj.put("category", task.category)
            tObj.put("categoryColorHex", task.categoryColorHex)
            tObj.put("recurrenceType", task.recurrenceType)
            tObj.put("customDaysOfWeek", task.customDaysOfWeek)
            tObj.put("startDate", task.startDate)
            tObj.put("endDate", task.endDate)
            tObj.put("reminderTime", task.reminderTime)
            tObj.put("createdAt", task.createdAt)
            tObj.put("isArchived", task.isArchived)
            tObj.put("note", task.note)
            tObj.put("autoCompleteWithSubtasks", task.autoCompleteWithSubtasks)
            tObj.put("pomodoroCount", task.pomodoroCount)
            tObj.put("isHabit", task.isHabit)
            tObj.put("blockedByTaskId", task.blockedByTaskId)
            tObj.put("reminderDays", task.reminderDays)
            tasksArray.put(tObj)
        }
        root.put("tasks", tasksArray)

        val subtasks = subtaskDao.getAllSubtasksDirect()
        val subtasksArray = JSONArray()
        for (sub in subtasks) {
            val sObj = JSONObject()
            sObj.put("id", sub.id)
            sObj.put("taskId", sub.taskId)
            sObj.put("title", sub.title)
            sObj.put("isCompleted", sub.isCompleted)
            sObj.put("orderIndex", sub.orderIndex)
            subtasksArray.put(sObj)
        }
        root.put("subtasks", subtasksArray)

        val logsArray = JSONArray()
        for (log in logs) {
            val lObj = JSONObject()
            lObj.put("id", log.id)
            lObj.put("taskId", log.taskId)
            lObj.put("date", log.date)
            lObj.put("completedAt", log.completedAt)
            logsArray.put(lObj)
        }
        root.put("completionLogs", logsArray)

        val categoriesArray = JSONArray()
        for (category in categories) {
            val cObj = JSONObject()
            cObj.put("name", category.name)
            cObj.put("colorHex", category.colorHex)
            cObj.put("iconName", category.iconName)
            categoriesArray.put(cObj)
        }
        root.put("categories", categoriesArray)

        return root.toString(2)
    }

    suspend fun exportDesktopJson(): String {
        val tasks = taskDao.getAllTasksDirect()
        val subtasks = subtaskDao.getAllSubtasksDirect()
        val logs = logDao.getAllLogsDirect()
        val categories = categoryDao.getAllCategoriesDirect()
        val chains = habitChainDao?.getAllChainsDirect() ?: emptyList()
        return com.abubakr.taskstreak.util.DesktopSyncBridge.exportForDesktop(
            tasks, subtasks, logs, categories, chains
        )
    }

    suspend fun exportMarkdown(): String {
        val tasks = taskDao.getAllTasksDirect()
        return com.abubakr.taskstreak.util.DesktopSyncBridge.exportToMarkdown(tasks)
    }

    suspend fun importDataFromJson(
        jsonString: String,
        mergeMode: Boolean = false,
        selectedTitles: Set<String>? = null
    ): Result<Int> {
        return try {
            val root = JSONObject(jsonString)
            val tasksArray = root.optJSONArray("tasks") ?: JSONArray()
            val logsArray = root.optJSONArray("completionLogs") ?: JSONArray()
            val categoriesArray = root.optJSONArray("categories") ?: JSONArray()

            val importedCategories = mutableListOf<CategoryEntity>()
            for (i in 0 until categoriesArray.length()) {
                val cObj = categoriesArray.getJSONObject(i)
                importedCategories.add(
                    CategoryEntity(
                        name = cObj.getString("name"),
                        colorHex = cObj.optString("colorHex", "#FF6B35"),
                        iconName = cObj.optString("iconName", "Bookmark")
                    )
                )
            }
            if (importedCategories.isNotEmpty()) {
                categoryDao.insertAll(importedCategories)
            }

            val taskIdMap = mutableMapOf<Long, Long>() // oldTaskId -> newTaskId

            var importedCount = 0
            for (i in 0 until tasksArray.length()) {
                val tObj = tasksArray.getJSONObject(i)
                val title = tObj.getString("title")

                if (selectedTitles != null && !selectedTitles.contains(title)) {
                    continue
                }

                val oldId = tObj.optLong("id", 0)
                val targetId = if (mergeMode) 0L else oldId

                val task = TaskEntity(
                    id = targetId,
                    title = title,
                    category = tObj.optString("category", "General"),
                    categoryColorHex = tObj.optString("categoryColorHex", "#FF6B35"),
                    recurrenceType = tObj.optString("recurrenceType", "DAILY"),
                    customDaysOfWeek = tObj.optString("customDaysOfWeek", "1,2,3,4,5,6,7"),
                    startDate = tObj.optString("startDate", DateUtils.todayString()),
                    endDate = if (tObj.isNull("endDate")) null else tObj.optString("endDate"),
                    reminderTime = if (tObj.isNull("reminderTime")) null else tObj.optString("reminderTime"),
                    createdAt = tObj.optLong("createdAt", System.currentTimeMillis()),
                    isArchived = tObj.optBoolean("isArchived", false),
                    note = if (tObj.isNull("note")) null else tObj.optString("note"),
                    autoCompleteWithSubtasks = tObj.optBoolean("autoCompleteWithSubtasks", true),
                    pomodoroCount = tObj.optInt("pomodoroCount", 0),
                    isHabit = tObj.optBoolean("isHabit", true),
                    blockedByTaskId = if (tObj.isNull("blockedByTaskId")) null else tObj.optLong("blockedByTaskId"),
                    reminderDays = if (tObj.isNull("reminderDays")) null else tObj.optString("reminderDays")
                )

                val newId = taskDao.insert(task)
                if (oldId > 0) {
                    taskIdMap[oldId] = newId
                }
                importedCount++
            }

            val subtasksArray = root.optJSONArray("subtasks")
            if (subtasksArray != null) {
                val importedSubtasks = mutableListOf<SubtaskEntity>()
                for (i in 0 until subtasksArray.length()) {
                    val sObj = subtasksArray.getJSONObject(i)
                    val oldTaskId = sObj.getLong("taskId")
                    val mappedTaskId = if (mergeMode) taskIdMap[oldTaskId] ?: oldTaskId else oldTaskId

                    importedSubtasks.add(
                        SubtaskEntity(
                            id = if (mergeMode) 0L else sObj.optLong("id", 0),
                            taskId = mappedTaskId,
                            title = sObj.getString("title"),
                            isCompleted = sObj.optBoolean("isCompleted", false),
                            orderIndex = sObj.optInt("orderIndex", 0)
                        )
                    )
                }
                if (importedSubtasks.isNotEmpty()) {
                    subtaskDao.insertAll(importedSubtasks)
                }
            }

            val importedLogs = mutableListOf<CompletionLogEntity>()
            for (i in 0 until logsArray.length()) {
                val lObj = logsArray.getJSONObject(i)
                val oldTaskId = lObj.getLong("taskId")
                val mappedTaskId = if (mergeMode) taskIdMap[oldTaskId] ?: oldTaskId else oldTaskId

                importedLogs.add(
                    CompletionLogEntity(
                        id = if (mergeMode) 0L else lObj.optLong("id", 0),
                        taskId = mappedTaskId,
                        date = lObj.getString("date"),
                        completedAt = lObj.optLong("completedAt", System.currentTimeMillis())
                    )
                )
            }
            if (importedLogs.isNotEmpty()) {
                logDao.insertAll(importedLogs)
            }

            Result.success(importedCount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun clearAllDatabaseData() {
        taskDao.deleteAllTasks()
        logDao.deleteAll()
        subtaskDao.deleteAllSubtasks()
        habitChainDao?.deleteAllChains()
        categoryDao.deleteAllCategories()

        // Re-seed default categories so user has clean slate
        val defaultCategories = listOf(
            CategoryEntity(name = "General", colorHex = "#FF6B35", iconName = "Bookmark"),
            CategoryEntity(name = "Study", colorHex = "#3B82F6", iconName = "MenuBook"),
            CategoryEntity(name = "English", colorHex = "#8B5CF6", iconName = "Translate"),
            CategoryEntity(name = "Personal", colorHex = "#EC4899", iconName = "Person"),
            CategoryEntity(name = "Fitness", colorHex = "#10B981", iconName = "FitnessCenter"),
            CategoryEntity(name = "Work", colorHex = "#F59E0B", iconName = "Work")
        )
        categoryDao.insertAll(defaultCategories)
    }
}
