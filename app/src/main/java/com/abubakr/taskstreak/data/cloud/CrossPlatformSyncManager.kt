package com.abubakr.taskstreak.data.cloud

import android.content.Context
import android.util.Log
import com.abubakr.taskstreak.data.model.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class CloudSyncRecord(
    val lastSyncedTimestamp: Long,
    val pendingPushesCount: Int,
    val isConnected: Boolean,
    val statusMessage: String
)

object CrossPlatformSyncManager {
    private const val TAG = "CrossPlatformSync"

    /**
     * Prepares structured JSON payload formatted for cross-platform Firestore/Cloud synchronization.
     * Compatible with Web, iOS, and Android clients.
     */
    fun buildSyncPayload(tasks: List<TaskEntity>, completedDatesMap: Map<Long, Set<String>>): String {
        val root = JSONObject()
        root.put("version", 2)
        root.put("platform", "android")
        root.put("syncedAt", System.currentTimeMillis())

        val tasksArray = JSONArray()
        tasks.forEach { task ->
            val taskObj = JSONObject().apply {
                put("id", task.id)
                put("title", task.title)
                put("category", task.category)
                put("categoryColorHex", task.categoryColorHex)
                put("recurrenceType", task.recurrenceType)
                put("reminderTime", task.reminderTime ?: "")
                put("createdAt", task.createdAt)
                put("isArchived", task.isArchived)

                val datesArr = JSONArray()
                completedDatesMap[task.id]?.forEach { datesArr.put(it) }
                put("completions", datesArr)
            }
            tasksArray.put(taskObj)
        }
        root.put("tasks", tasksArray)
        return root.toString(2)
    }

    /**
     * Resolves sync conflicts using Last-Write-Wins (LWW) strategy based on task createdAt and timestamp.
     */
    fun resolveConflicts(
        localTasks: List<TaskEntity>,
        remotePayloadJson: String
    ): List<TaskEntity> {
        return try {
            val root = JSONObject(remotePayloadJson)
            val remoteTasksArray = root.optJSONArray("tasks") ?: return localTasks
            val mergedMap = localTasks.associateBy { it.id }.toMutableMap()

            for (i in 0 until remoteTasksArray.length()) {
                val obj = remoteTasksArray.getJSONObject(i)
                val id = obj.optLong("id")
                val title = obj.optString("title")
                val category = obj.optString("category", "General")
                val isArchived = obj.optBoolean("isArchived", false)
                val createdAt = obj.optLong("createdAt", System.currentTimeMillis())

                val existing = mergedMap[id]
                if (existing == null) {
                    // New remote task
                    mergedMap[id] = TaskEntity(
                        id = id,
                        title = title,
                        category = category,
                        isArchived = isArchived,
                        createdAt = createdAt
                    )
                } else if (createdAt > existing.createdAt) {
                    // Remote has newer record
                    mergedMap[id] = existing.copy(
                        title = title,
                        category = category,
                        isArchived = isArchived
                    )
                }
            }
            mergedMap.values.toList()
        } catch (e: Exception) {
            Log.e(TAG, "Error resolving sync conflicts", e)
            localTasks
        }
    }
}
