package com.abubakr.taskstreak.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "completion_logs",
    indices = [
        Index(value = ["taskId", "date"], unique = true),
        Index(value = ["date"]),
        Index(value = ["taskId"])
    ]
)
data class CompletionLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val taskId: Long,
    val date: String, // "yyyy-MM-dd"
    val completedAt: Long = System.currentTimeMillis()
)
