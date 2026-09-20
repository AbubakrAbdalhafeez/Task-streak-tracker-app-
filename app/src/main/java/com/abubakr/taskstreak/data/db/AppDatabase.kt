package com.abubakr.taskstreak.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.abubakr.taskstreak.data.model.CategoryEntity
import com.abubakr.taskstreak.data.model.CompletionLogEntity
import com.abubakr.taskstreak.data.model.HabitChainEntity
import com.abubakr.taskstreak.data.model.SubtaskEntity
import com.abubakr.taskstreak.data.model.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

@Database(
    entities = [
        TaskEntity::class,
        CompletionLogEntity::class,
        CategoryEntity::class,
        SubtaskEntity::class,
        HabitChainEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun completionLogDao(): CompletionLogDao
    abstract fun categoryDao(): CategoryDao
    abstract fun subtaskDao(): SubtaskDao
    abstract fun habitChainDao(): HabitChainDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "task_streak_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                // Seed initial categories and starter habits
                CoroutineScope(Dispatchers.IO).launch {
                    val database = getDatabase(context)
                    val categoryDao = database.categoryDao()
                    val taskDao = database.taskDao()
                    val logDao = database.completionLogDao()

                    val defaultCategories = listOf(
                        CategoryEntity(name = "General", colorHex = "#FF6B35", iconName = "Bookmark"),
                        CategoryEntity(name = "Study", colorHex = "#3B82F6", iconName = "MenuBook"),
                        CategoryEntity(name = "English", colorHex = "#8B5CF6", iconName = "Translate"),
                        CategoryEntity(name = "Personal", colorHex = "#EC4899", iconName = "Person"),
                        CategoryEntity(name = "Fitness", colorHex = "#10B981", iconName = "FitnessCenter"),
                        CategoryEntity(name = "Work", colorHex = "#F59E0B", iconName = "Work")
                    )
                    categoryDao.insertAll(defaultCategories)

                    val today = LocalDate.now()
                    val twoWeeksAgo = today.minusDays(14).toString()

                    val task1 = TaskEntity(
                        id = 1,
                        title = "Daily Workout & Movement",
                        category = "Fitness",
                        categoryColorHex = "#10B981",
                        recurrenceType = "DAILY",
                        startDate = twoWeeksAgo,
                        reminderTime = "07:30"
                    )
                    val task2 = TaskEntity(
                        id = 2,
                        title = "English Reading (15 mins)",
                        category = "English",
                        categoryColorHex = "#8B5CF6",
                        recurrenceType = "DAILY",
                        startDate = twoWeeksAgo,
                        reminderTime = "19:00"
                    )
                    val task3 = TaskEntity(
                        id = 3,
                        title = "Study & Code Practice",
                        category = "Study",
                        categoryColorHex = "#3B82F6",
                        recurrenceType = "CUSTOM",
                        customDaysOfWeek = "1,2,3,4,5", // Mon-Fri
                        startDate = twoWeeksAgo,
                        reminderTime = "20:00"
                    )

                    taskDao.insertAll(listOf(task1, task2, task3))

                    // Seed some initial completion logs for nice streaks
                    val logs = mutableListOf<CompletionLogEntity>()
                    // Task 1: completed 6 days up to yesterday
                    for (i in 1..6) {
                        logs.add(
                            CompletionLogEntity(
                                taskId = 1,
                                date = today.minusDays(i.toLong()).toString()
                            )
                        )
                    }
                    // Task 2: completed 3 days
                    for (i in 1..3) {
                        logs.add(
                            CompletionLogEntity(
                                taskId = 2,
                                date = today.minusDays(i.toLong()).toString()
                            )
                        )
                    }
                    logDao.insertAll(logs)
                }
            }
        }
    }
}
