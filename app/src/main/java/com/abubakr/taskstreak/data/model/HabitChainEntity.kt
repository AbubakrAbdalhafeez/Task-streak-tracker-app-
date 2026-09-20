package com.abubakr.taskstreak.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habit_chains")
data class HabitChainEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val taskIds: String = "", // Comma-separated Task IDs: "1,2,5"
    val colorHex: String = "#FF6B35",
    val iconName: String = "Link",
    val bonusXp: Int = 50,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getParsedTaskIds(): List<Long> {
        return taskIds.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
    }
}
