package com.abubakr.taskstreak.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.abubakr.taskstreak.data.model.CompletionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionLogDao {
    @Query("SELECT * FROM completion_logs")
    fun getAllLogs(): Flow<List<CompletionLogEntity>>

    @Query("SELECT * FROM completion_logs")
    suspend fun getAllLogsDirect(): List<CompletionLogEntity>

    @Query("SELECT * FROM completion_logs WHERE taskId = :taskId")
    fun getLogsForTask(taskId: Long): Flow<List<CompletionLogEntity>>

    @Query("SELECT * FROM completion_logs WHERE taskId = :taskId")
    suspend fun getLogsForTaskDirect(taskId: Long): List<CompletionLogEntity>

    @Query("SELECT * FROM completion_logs WHERE date = :date")
    fun getLogsForDate(date: String): Flow<List<CompletionLogEntity>>

    @Query("SELECT * FROM completion_logs WHERE taskId = :taskId AND date = :date LIMIT 1")
    suspend fun getLog(taskId: Long, date: String): CompletionLogEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: CompletionLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<CompletionLogEntity>)

    @Query("DELETE FROM completion_logs WHERE taskId = :taskId AND date = :date")
    suspend fun delete(taskId: Long, date: String)

    @Query("DELETE FROM completion_logs WHERE taskId = :taskId")
    suspend fun deleteForTask(taskId: Long)

    @Query("DELETE FROM completion_logs")
    suspend fun deleteAll()
}
