package com.abubakr.taskstreak.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.abubakr.taskstreak.data.model.HabitChainEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitChainDao {
    @Query("SELECT * FROM habit_chains ORDER BY createdAt DESC")
    fun getAllChains(): Flow<List<HabitChainEntity>>

    @Query("SELECT * FROM habit_chains ORDER BY createdAt DESC")
    suspend fun getAllChainsDirect(): List<HabitChainEntity>

    @Query("SELECT * FROM habit_chains WHERE id = :id")
    suspend fun getChainById(id: Long): HabitChainEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(chain: HabitChainEntity): Long

    @Update
    suspend fun update(chain: HabitChainEntity)

    @Delete
    suspend fun delete(chain: HabitChainEntity)

    @Query("DELETE FROM habit_chains WHERE id = :id")
    suspend fun deleteById(id: Long)
}
