package com.abubakr.taskstreak.data.backup

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.abubakr.taskstreak.data.db.AppDatabase
import com.abubakr.taskstreak.data.preferences.SettingsPreferences
import com.abubakr.taskstreak.data.repository.StreakRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class LocalAutoBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val WORK_NAME = "local_auto_backup_work"
        private const val BACKUP_PREFIX = "task-streak-backup-"
        private const val BACKUP_EXTENSION = ".json"
        const val MAX_BACKUPS_TO_KEEP = 4

        fun schedulePeriodicBackup(context: Context) {
            val constraints = Constraints.Builder()
                .build()

            val workRequest = PeriodicWorkRequestBuilder<LocalAutoBackupWorker>(7, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }

        fun cancelPeriodicBackup(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }

        /**
         * Saves backup JSON string to Downloads folder and rotates older backups so only the last 4 remain.
         */
        suspend fun performBackupToDownloads(context: Context, jsonContent: String): kotlin.Result<String> = withContext(Dispatchers.IO) {
            try {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val todayStr = dateFormat.format(Date())
                val fileName = "$BACKUP_PREFIX$todayStr$BACKUP_EXTENSION"

                var savedSuccessfully = false

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }

                    val uri: Uri? = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { stream ->
                            stream.write(jsonContent.toByteArray(Charsets.UTF_8))
                            stream.flush()
                            savedSuccessfully = true
                        }
                    }
                }

                if (!savedSuccessfully) {
                    // Fallback using Downloads folder directly
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    if (!downloadsDir.exists()) {
                        downloadsDir.mkdirs()
                    }
                    val targetFile = File(downloadsDir, fileName)
                    FileOutputStream(targetFile).use { stream ->
                        stream.write(jsonContent.toByteArray(Charsets.UTF_8))
                        stream.flush()
                        savedSuccessfully = true
                    }
                }

                // Perform rotation: Keep only the newest 4 backups
                rotateOldBackups(context)

                kotlin.Result.success(fileName)
            } catch (e: Exception) {
                kotlin.Result.failure(e)
            }
        }

        /**
         * Scans for task-streak-backup-*.json in Downloads and removes files older than the last 4.
         */
        private fun rotateOldBackups(context: Context) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = context.contentResolver
                    val projection = arrayOf(
                        MediaStore.MediaColumns._ID,
                        MediaStore.MediaColumns.DISPLAY_NAME,
                        MediaStore.MediaColumns.DATE_MODIFIED
                    )
                    val selection = "${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ? AND ${MediaStore.MediaColumns.DISPLAY_NAME} LIKE ?"
                    val selectionArgs = arrayOf("$BACKUP_PREFIX%", "%$BACKUP_EXTENSION")
                    val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"

                    val cursor = resolver.query(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        sortOrder
                    )

                    cursor?.use {
                        val idColumn = it.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                        var count = 0
                        while (it.moveToNext()) {
                            count++
                            if (count > MAX_BACKUPS_TO_KEEP) {
                                val id = it.getLong(idColumn)
                                val deleteUri = Uri.withAppendedPath(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id.toString())
                                resolver.delete(deleteUri, null, null)
                            }
                        }
                    }
                }

                // Also clean up file-based downloads directory
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                if (downloadsDir.exists() && downloadsDir.isDirectory) {
                    val matchingFiles = downloadsDir.listFiles { file ->
                        file.isFile && file.name.startsWith(BACKUP_PREFIX) && file.name.endsWith(BACKUP_EXTENSION)
                    } ?: emptyArray()

                    if (matchingFiles.size > MAX_BACKUPS_TO_KEEP) {
                        matchingFiles.sortByDescending { it.lastModified() }
                        for (i in MAX_BACKUPS_TO_KEEP until matchingFiles.size) {
                            matchingFiles[i].delete()
                        }
                    }
                }
            } catch (_: Exception) {
                // Rotation failure shouldn't fail the backup
            }
        }
    }

    override suspend fun doWork(): Result {
        val prefs = SettingsPreferences(applicationContext)
        if (!prefs.autoBackupLocalEnabled.value) {
            return Result.success()
        }

        val db = AppDatabase.getDatabase(applicationContext)
        val repository = StreakRepository(db.taskDao(), db.completionLogDao(), db.categoryDao(), db.subtaskDao())

        return try {
            val jsonContent = repository.exportDataAsJson()
            val backupResult = performBackupToDownloads(applicationContext, jsonContent)
            if (backupResult.isSuccess) {
                prefs.setLastLocalBackupTime(System.currentTimeMillis())
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
