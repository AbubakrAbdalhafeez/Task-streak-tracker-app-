package com.abubakr.taskstreak.util

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.abubakr.taskstreak.data.db.AppDatabase
import com.abubakr.taskstreak.data.model.CompletionLogEntity
import com.abubakr.taskstreak.data.model.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

object WearCompanionBridge {
    const val ACTION_WEAR_COMPLETE_TASK = "com.abubakr.taskstreak.ACTION_WEAR_COMPLETE_TASK"
    const val EXTRA_TASK_ID = "extra_wear_task_id"

    /**
     * Serializes today's tasks into a light JSON packet suitable for Wear OS watch data layers
     */
    fun buildWearDataPacket(tasks: List<TaskEntity>, completedTaskIds: Set<Long>): String {
        val root = JSONObject()
        root.put("timestamp", System.currentTimeMillis())
        root.put("todayDate", DateUtils.todayString())

        val array = JSONArray()
        tasks.filter { !it.isArchived }.forEach { task ->
            val obj = JSONObject()
            obj.put("id", task.id)
            obj.put("title", task.title)
            obj.put("isCompleted", completedTaskIds.contains(task.id))
            obj.put("category", task.category)
            array.put(obj)
        }
        root.put("tasks", array)
        return root.toString()
    }
}

class WearSyncReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == WearCompanionBridge.ACTION_WEAR_COMPLETE_TASK) {
            val taskId = intent.getLongExtra(WearCompanionBridge.EXTRA_TASK_ID, -1L)
            if (taskId > 0) {
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val db = AppDatabase.getDatabase(context)
                        val today = DateUtils.todayString()
                        val isDone = db.completionLogDao().getLog(taskId, today) != null
                        if (!isDone) {
                            db.completionLogDao().insert(CompletionLogEntity(taskId = taskId, date = today))
                        }
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
