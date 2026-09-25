package com.abubakr.taskstreak.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.abubakr.taskstreak.data.model.SubtaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtaskDao {
    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY orderIndex ASC, id ASC")
    fun getSubtasksForTask(taskId: Long): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtasks WHERE taskId = :taskId ORDER BY orderIndex ASC, id ASC")
    suspend fun getSubtasksForTaskDirect(taskId: Long): List<SubtaskEntity>

    @Query("SELECT * FROM subtasks ORDER BY orderIndex ASC, id ASC")
    fun getAllSubtasks(): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtasks ORDER BY orderIndex ASC, id ASC")
    suspend fun getAllSubtasksDirect(): List<SubtaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subtask: SubtaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subtasks: List<SubtaskEntity>)

    @Update
    suspend fun update(subtask: SubtaskEntity)

    @Delete
    suspend fun delete(subtask: SubtaskEntity)

    @Query("DELETE FROM subtasks WHERE taskId = :taskId")
    suspend fun deleteSubtasksForTask(taskId: Long)

    @Query("DELETE FROM subtasks")
    suspend fun deleteAllSubtasks()

    @Query("UPDATE subtasks SET isCompleted = :isCompleted WHERE id = :id")
    suspend fun setSubtaskCompleted(id: Long, isCompleted: Boolean)
}
