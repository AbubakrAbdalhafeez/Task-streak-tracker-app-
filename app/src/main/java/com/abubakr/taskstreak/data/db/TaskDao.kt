package com.abubakr.taskstreak.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.abubakr.taskstreak.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    suspend fun getAllTasksDirect(): List<TaskEntity>

    @Query("SELECT * FROM tasks WHERE id = :id")
    fun getTaskById(id: Long): Flow<TaskEntity?>

    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskByIdDirect(id: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE isArchived = 1 ORDER BY createdAt DESC")
    fun getArchivedTasks(): Flow<List<TaskEntity>>

    @Query("UPDATE tasks SET isArchived = 1 WHERE id = :id")
    suspend fun archiveTask(id: Long)

    @Query("UPDATE tasks SET isArchived = 0 WHERE id = :id")
    suspend fun unarchiveTask(id: Long)

    @Query("DELETE FROM tasks WHERE isArchived = 1")
    suspend fun deleteArchivedTasks()

    @Query("DELETE FROM tasks")
    suspend fun deleteAllTasks()

    @Query("SELECT * FROM tasks WHERE id IN (:ids)")
    suspend fun getTasksByIds(ids: List<Long>): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>): List<Long>

    @Update
    suspend fun update(task: TaskEntity)

    @Delete
    suspend fun delete(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: Long)
}
