package com.abubakr.taskstreak.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("streak_tracker_prefs", Context.MODE_PRIVATE)

    private val _dailyGoal = MutableStateFlow(prefs.getInt("daily_goal", 3))
    val dailyGoal: StateFlow<Int> = _dailyGoal.asStateFlow()

    private val _themeMode = MutableStateFlow(prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _morningReminderEnabled =
        MutableStateFlow(prefs.getBoolean("morning_reminder_enabled", false))
    val morningReminderEnabled: StateFlow<Boolean> = _morningReminderEnabled.asStateFlow()

    private val _morningReminderTime =
        MutableStateFlow(prefs.getString("morning_reminder_time", "09:00") ?: "09:00")
    val morningReminderTime: StateFlow<String> = _morningReminderTime.asStateFlow()

    private val _reminderOnlyIfPending =
        MutableStateFlow(prefs.getBoolean("reminder_only_if_pending", true))
    val reminderOnlyIfPending: StateFlow<Boolean> = _reminderOnlyIfPending.asStateFlow()

    private val _driveAutoSync =
        MutableStateFlow(prefs.getBoolean("drive_auto_sync", false))
    val driveAutoSync: StateFlow<Boolean> = _driveAutoSync.asStateFlow()

    private val _driveLastSyncedTime =
        MutableStateFlow(prefs.getLong("drive_last_synced_time", 0L))
    val driveLastSyncedTime: StateFlow<Long> = _driveLastSyncedTime.asStateFlow()

    private val _driveUserEmail =
        MutableStateFlow(prefs.getString("drive_user_email", null))
    val driveUserEmail: StateFlow<String?> = _driveUserEmail.asStateFlow()

    private val _driveUserName =
        MutableStateFlow(prefs.getString("drive_user_name", null))
    val driveUserName: StateFlow<String?> = _driveUserName.asStateFlow()

    private val _driveUserPhoto =
        MutableStateFlow(prefs.getString("drive_user_photo", null))
    val driveUserPhoto: StateFlow<String?> = _driveUserPhoto.asStateFlow()

    private val _autoBackupLocalEnabled =
        MutableStateFlow(prefs.getBoolean("auto_backup_local_enabled", true))
    val autoBackupLocalEnabled: StateFlow<Boolean> = _autoBackupLocalEnabled.asStateFlow()

    private val _lastLocalBackupTime =
        MutableStateFlow(prefs.getLong("last_local_backup_time", 0L))
    val lastLocalBackupTime: StateFlow<Long> = _lastLocalBackupTime.asStateFlow()

    private val _colorPalette =
        MutableStateFlow(prefs.getString("color_palette", "FLAME") ?: "FLAME")
    val colorPalette: StateFlow<String> = _colorPalette.asStateFlow()

    private val _appLanguage =
        MutableStateFlow(prefs.getString("app_language", "SYSTEM") ?: "SYSTEM")
    val appLanguage: StateFlow<String> = _appLanguage.asStateFlow()

    private val _soundEnabled =
        MutableStateFlow(prefs.getBoolean("sound_effects_enabled", true))
    val soundEnabled: StateFlow<Boolean> = _soundEnabled.asStateFlow()

    private val _hapticsEnabled =
        MutableStateFlow(prefs.getBoolean("haptics_enabled", true))
    val hapticsEnabled: StateFlow<Boolean> = _hapticsEnabled.asStateFlow()

    private val _vacationMode =
        MutableStateFlow(prefs.getBoolean("vacation_mode", false))
    val vacationMode: StateFlow<Boolean> = _vacationMode.asStateFlow()

    fun setColorPalette(palette: String) {
        prefs.edit().putString("color_palette", palette).apply()
        _colorPalette.value = palette
    }

    fun setAppLanguage(lang: String) {
        prefs.edit().putString("app_language", lang).apply()
        _appLanguage.value = lang
    }

    fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("sound_effects_enabled", enabled).apply()
        _soundEnabled.value = enabled
    }

    fun setHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("haptics_enabled", enabled).apply()
        _hapticsEnabled.value = enabled
    }

    fun setVacationMode(enabled: Boolean) {
        prefs.edit().putBoolean("vacation_mode", enabled).apply()
        _vacationMode.value = enabled
    }

    fun setDailyGoal(goal: Int) {
        val g = goal.coerceAtLeast(1)
        prefs.edit().putInt("daily_goal", g).apply()
        _dailyGoal.value = g
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _themeMode.value = mode
    }

    fun setMorningReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("morning_reminder_enabled", enabled).apply()
        _morningReminderEnabled.value = enabled
    }

    fun setMorningReminderTime(time: String) {
        prefs.edit().putString("morning_reminder_time", time).apply()
        _morningReminderTime.value = time
    }

    fun setReminderOnlyIfPending(onlyIfPending: Boolean) {
        prefs.edit().putBoolean("reminder_only_if_pending", onlyIfPending).apply()
        _reminderOnlyIfPending.value = onlyIfPending
    }

    fun setDriveAutoSync(enabled: Boolean) {
        prefs.edit().putBoolean("drive_auto_sync", enabled).apply()
        _driveAutoSync.value = enabled
    }

    fun setDriveLastSyncedTime(time: Long) {
        prefs.edit().putLong("drive_last_synced_time", time).apply()
        _driveLastSyncedTime.value = time
    }

    fun setDriveUserInfo(email: String?, name: String?, photoUrl: String?) {
        prefs.edit()
            .putString("drive_user_email", email)
            .putString("drive_user_name", name)
            .putString("drive_user_photo", photoUrl)
            .apply()
        _driveUserEmail.value = email
        _driveUserName.value = name
        _driveUserPhoto.value = photoUrl
    }

    fun clearDriveUserInfo() {
        prefs.edit()
            .remove("drive_user_email")
            .remove("drive_user_name")
            .remove("drive_user_photo")
            .remove("drive_auto_sync")
            .remove("drive_last_synced_time")
            .apply()
        _driveUserEmail.value = null
        _driveUserName.value = null
        _driveUserPhoto.value = null
        _driveAutoSync.value = false
        _driveLastSyncedTime.value = 0L
    }

    fun setAutoBackupLocalEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("auto_backup_local_enabled", enabled).apply()
        _autoBackupLocalEnabled.value = enabled
    }

    fun setLastLocalBackupTime(time: Long) {
        prefs.edit().putLong("last_local_backup_time", time).apply()
        _lastLocalBackupTime.value = time
    }
}
