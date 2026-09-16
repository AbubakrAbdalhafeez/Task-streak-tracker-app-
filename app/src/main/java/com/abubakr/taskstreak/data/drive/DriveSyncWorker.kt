package com.abubakr.taskstreak.data.drive

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.abubakr.taskstreak.data.db.AppDatabase
import com.abubakr.taskstreak.data.preferences.SettingsPreferences
import com.abubakr.taskstreak.data.repository.StreakRepository
import java.util.concurrent.TimeUnit

class DriveSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "google_drive_auto_sync_work"

        fun scheduleAutoSync(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = PeriodicWorkRequestBuilder<DriveSyncWorker>(24, TimeUnit.HOURS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                syncRequest
            )
        }

        fun cancelAutoSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        val prefs = SettingsPreferences(applicationContext)
        if (!prefs.driveAutoSync.value) {
            return Result.success()
        }

        val driveManager = GoogleDriveManager(applicationContext)
        if (driveManager.getSignedInAccount() == null) {
            return Result.success()
        }

        val db = AppDatabase.getDatabase(applicationContext)
        val repository = StreakRepository(db.taskDao(), db.completionLogDao(), db.categoryDao(), db.subtaskDao())

        return try {
            val jsonContent = repository.exportDataAsJson()
            val backupResult = driveManager.backupData(jsonContent)
            if (backupResult.isSuccess) {
                prefs.setDriveLastSyncedTime(System.currentTimeMillis())
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
