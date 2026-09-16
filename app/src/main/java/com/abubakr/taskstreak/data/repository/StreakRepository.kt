package com.abubakr.taskstreak.data.repository

import com.abubakr.taskstreak.data.db.CategoryDao
import com.abubakr.taskstreak.data.db.CompletionLogDao
import com.abubakr.taskstreak.data.db.SubtaskDao
import com.abubakr.taskstreak.data.db.TaskDao
import com.abubakr.taskstreak.data.model.CategoryEntity
import com.abubakr.taskstreak.data.model.CompletionLogEntity
import com.abubakr.taskstreak.data.model.SubtaskEntity
import com.abubakr.taskstreak.data.model.TaskEntity
import com.abubakr.taskstreak.util.DateUtils
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class StreakRepository(
    private val taskDao: TaskDao,
    private val logDao: CompletionLogDao,
    private val categoryDao: CategoryDao,
    private val subtaskDao: SubtaskDao
) {
    val allActiveTasks: Flow<List<TaskEntity>> = taskDao.getAllActiveTasks()
    val allLogs: Flow<List<CompletionLogEntity>> = logDao.getAllLogs()
    val allCategories: Flow<List<CategoryEntity>> = categoryDao.getAllCategories()
    val allSubtasks: Flow<List<SubtaskEntity>> = subtaskDao.getAllSubtasks()

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

    suspend fun addCategory(category: CategoryEntity): Long = categoryDao.insert(category)

    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.delete(category)

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

    suspend fun importDataFromJson(jsonString: String): Result<Int> {
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

            val importedTasks = mutableListOf<TaskEntity>()
            for (i in 0 until tasksArray.length()) {
                val tObj = tasksArray.getJSONObject(i)
                importedTasks.add(
                    TaskEntity(
                        id = tObj.optLong("id", 0),
                        title = tObj.getString("title"),
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
                )
            }
            if (importedTasks.isNotEmpty()) {
                taskDao.insertAll(importedTasks)
            }

            val subtasksArray = root.optJSONArray("subtasks")
            if (subtasksArray != null) {
                val importedSubtasks = mutableListOf<SubtaskEntity>()
                for (i in 0 until subtasksArray.length()) {
                    val sObj = subtasksArray.getJSONObject(i)
                    importedSubtasks.add(
                        SubtaskEntity(
                            id = sObj.optLong("id", 0),
                            taskId = sObj.getLong("taskId"),
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
                importedLogs.add(
                    CompletionLogEntity(
                        id = lObj.optLong("id", 0),
                        taskId = lObj.getLong("taskId"),
                        date = lObj.getString("date"),
                        completedAt = lObj.optLong("completedAt", System.currentTimeMillis())
                    )
                )
            }
            if (importedLogs.isNotEmpty()) {
                logDao.insertAll(importedLogs)
            }

            Result.success(importedTasks.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
