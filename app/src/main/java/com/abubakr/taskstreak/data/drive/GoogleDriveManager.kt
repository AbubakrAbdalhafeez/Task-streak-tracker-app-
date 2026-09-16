package com.abubakr.taskstreak.data.drive

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.Collections

sealed class DriveSyncState {
    object Idle : DriveSyncState()
    object Syncing : DriveSyncState()
    data class Synced(val message: String, val timestamp: Long = System.currentTimeMillis()) : DriveSyncState()
    data class Error(val message: String) : DriveSyncState()
}

class GoogleDriveManager(private val context: Context) {

    companion object {
        const val BACKUP_FILE_NAME = "task-streak-backup.json"
        const val MIME_TYPE_JSON = "application/json"
        const val MIME_TYPE_FOLDER = "application/vnd.google-apps.folder"
        const val FOLDER_NAME = "TaskStreakTracker"
    }

    private val signInOptions: GoogleSignInOptions by lazy {
        GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestProfile()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
    }

    private val googleSignInClient: GoogleSignInClient by lazy {
        GoogleSignIn.getClient(context, signInOptions)
    }

    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    fun getSignedInAccount(): GoogleSignInAccount? {
        val account = GoogleSignIn.getLastSignedInAccount(context)
        return if (account != null && GoogleSignIn.hasPermissions(account, Scope(DriveScopes.DRIVE_FILE))) {
            account
        } else {
            null
        }
    }

    suspend fun signOut(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            googleSignInClient.signOut()
            googleSignInClient.revokeAccess()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun isOnline(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun getDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            Collections.singletonList(DriveScopes.DRIVE_FILE)
        )
        credential.selectedAccount = account.account

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Task Streak Tracker")
            .build()
    }

    suspend fun backupData(jsonContent: String): Result<String> = withContext(Dispatchers.IO) {
        if (!isOnline()) {
            return@withContext Result.failure(Exception("Offline: Check your internet connection."))
        }

        val account = getSignedInAccount()
            ?: return@withContext Result.failure(Exception("Not signed in to Google Drive."))

        try {
            val drive = getDriveService(account)

            // Find or create app-specific folder
            val folderId = getOrCreateAppFolder(drive)

            // Search for existing backup file
            val existingFile = findFileInFolder(drive, BACKUP_FILE_NAME, folderId)

            val contentBytes = jsonContent.toByteArray(Charsets.UTF_8)
            val mediaContent = ByteArrayContent(MIME_TYPE_JSON, contentBytes)

            if (existingFile != null) {
                // Update existing file
                val fileMetadata = File().apply {
                    name = BACKUP_FILE_NAME
                }
                drive.files().update(existingFile.id, fileMetadata, mediaContent).execute()
                Result.success("Backup successfully updated on Google Drive.")
            } else {
                // Create new file inside folder
                val fileMetadata = File().apply {
                    name = BACKUP_FILE_NAME
                    parents = listOf(folderId)
                }
                drive.files().create(fileMetadata, mediaContent).execute()
                Result.success("Backup successfully uploaded to Google Drive.")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreData(): Result<String> = withContext(Dispatchers.IO) {
        if (!isOnline()) {
            return@withContext Result.failure(Exception("Offline: Check your internet connection."))
        }

        val account = getSignedInAccount()
            ?: return@withContext Result.failure(Exception("Not signed in to Google Drive."))

        try {
            val drive = getDriveService(account)
            val folderId = getOrCreateAppFolder(drive)
            val existingFile = findFileInFolder(drive, BACKUP_FILE_NAME, folderId)
                ?: return@withContext Result.failure(Exception("No backup found on Google Drive ($BACKUP_FILE_NAME)."))

            val outputStream = ByteArrayOutputStream()
            drive.files().get(existingFile.id).executeMediaAndDownloadTo(outputStream)
            val jsonContent = outputStream.toString(Charsets.UTF_8.name())
            Result.success(jsonContent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getOrCreateAppFolder(drive: Drive): String {
        val query = "mimeType = '$MIME_TYPE_FOLDER' and name = '$FOLDER_NAME' and trashed = false"
        val result = drive.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name)")
            .execute()

        val files = result.files
        if (!files.isNullOrEmpty()) {
            return files[0].id
        }

        // Create folder
        val folderMetadata = File().apply {
            name = FOLDER_NAME
            mimeType = MIME_TYPE_FOLDER
        }
        val createdFolder = drive.files().create(folderMetadata)
            .setFields("id")
            .execute()

        return createdFolder.id
    }

    private fun findFileInFolder(drive: Drive, fileName: String, folderId: String): File? {
        val query = "name = '$fileName' and '$folderId' in parents and trashed = false"
        val result = drive.files().list()
            .setQ(query)
            .setSpaces("drive")
            .setFields("files(id, name, modifiedTime)")
            .execute()

        val files = result.files
        return if (!files.isNullOrEmpty()) files[0] else null
    }
}
