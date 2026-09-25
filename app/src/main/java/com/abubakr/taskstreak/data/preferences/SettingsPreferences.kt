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
        MutableStateFlow(
            prefs.getString("app_language", null).let { saved ->
                if (saved in listOf("ar", "en")) saved!!
                else if (java.util.Locale.getDefault().language == "ar") "ar"
                else "en"
            }
        )
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

    private val _quietHoursEnabled =
        MutableStateFlow(prefs.getBoolean("quiet_hours_enabled", false))
    val quietHoursEnabled: StateFlow<Boolean> = _quietHoursEnabled.asStateFlow()

    private val _quietHoursStart =
        MutableStateFlow(prefs.getString("quiet_hours_start", "22:00") ?: "22:00")
    val quietHoursStart: StateFlow<String> = _quietHoursStart.asStateFlow()

    private val _quietHoursEnd =
        MutableStateFlow(prefs.getString("quiet_hours_end", "07:00") ?: "07:00")
    val quietHoursEnd: StateFlow<String> = _quietHoursEnd.asStateFlow()

    // Gamification (Feature 34)
    private val _totalXp = MutableStateFlow(prefs.getLong("user_total_xp", 120L))
    val totalXp: StateFlow<Long> = _totalXp.asStateFlow()

    private val _streakShields = MutableStateFlow(prefs.getInt("streak_shields_count", 2))
    val streakShields: StateFlow<Int> = _streakShields.asStateFlow()

    private val _streakShieldsUsed = MutableStateFlow(prefs.getInt("streak_shields_used", 0))
    val streakShieldsUsed: StateFlow<Int> = _streakShieldsUsed.asStateFlow()

    // Accessibility (Feature 26)
    private val _highContrastEnabled = MutableStateFlow(prefs.getBoolean("high_contrast_enabled", false))
    val highContrastEnabled: StateFlow<Boolean> = _highContrastEnabled.asStateFlow()

    private val _largeTextScaleEnabled = MutableStateFlow(prefs.getBoolean("large_text_scale_enabled", false))
    val largeTextScaleEnabled: StateFlow<Boolean> = _largeTextScaleEnabled.asStateFlow()

    private val _colorblindMode = MutableStateFlow(prefs.getString("colorblind_mode", "NONE") ?: "NONE")
    val colorblindMode: StateFlow<String> = _colorblindMode.asStateFlow()

    // Task Archive settings (Feature 22)
    private val _autoArchiveDays = MutableStateFlow(prefs.getInt("auto_archive_days", 0)) // 0 = disabled
    val autoArchiveDays: StateFlow<Int> = _autoArchiveDays.asStateFlow()

    // Categories (Feature 23)
    private val _hideUnusedCategories = MutableStateFlow(prefs.getBoolean("hide_unused_categories", false))
    val hideUnusedCategories: StateFlow<Boolean> = _hideUnusedCategories.asStateFlow()

    // Smart Notifications (Feature 30)
    private val _smartNotificationsEnabled = MutableStateFlow(prefs.getBoolean("smart_notifications_enabled", true))
    val smartNotificationsEnabled: StateFlow<Boolean> = _smartNotificationsEnabled.asStateFlow()

    // Onboarding (Feature 41)
    private val _onboardingCompleted = MutableStateFlow(prefs.getBoolean("onboarding_completed", false))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    // Time Zone Travel Mode (Feature 49)
    private val _travelModeEnabled = MutableStateFlow(prefs.getBoolean("travel_mode_enabled", false))
    val travelModeEnabled: StateFlow<Boolean> = _travelModeEnabled.asStateFlow()

    // Encrypted Backup password (Feature 50)
    private val _hasBackupPassword = MutableStateFlow(prefs.getBoolean("has_backup_password", false))
    val hasBackupPassword: StateFlow<Boolean> = _hasBackupPassword.asStateFlow()

    // Google Fit / Health Sync (Feature 55)
    private val _googleFitSyncEnabled = MutableStateFlow(prefs.getBoolean("google_fit_sync_enabled", false))
    val googleFitSyncEnabled: StateFlow<Boolean> = _googleFitSyncEnabled.asStateFlow()

    // Pomodoro & Focus Settings
    private val _partialCreditEnabled = MutableStateFlow(prefs.getBoolean("focus_partial_credit_enabled", true))
    val partialCreditEnabled: StateFlow<Boolean> = _partialCreditEnabled.asStateFlow()

    private val _focusLastDurationMinutes = MutableStateFlow(prefs.getInt("focus_last_duration_minutes", 25))
    val focusLastDurationMinutes: StateFlow<Int> = _focusLastDurationMinutes.asStateFlow()

    private val _focusSessionMode = MutableStateFlow(prefs.getString("focus_session_mode", "CLASSIC") ?: "CLASSIC")
    val focusSessionMode: StateFlow<String> = _focusSessionMode.asStateFlow()

    private val _focusAutoStartType = MutableStateFlow(prefs.getString("focus_auto_start_type", "MANUAL") ?: "MANUAL")
    val focusAutoStartType: StateFlow<String> = _focusAutoStartType.asStateFlow()

    private val _focusOneMinuteWarningEnabled = MutableStateFlow(prefs.getBoolean("focus_one_min_warning", true))
    val focusOneMinuteWarningEnabled: StateFlow<Boolean> = _focusOneMinuteWarningEnabled.asStateFlow()

    private val _focusCompletionNotificationEnabled = MutableStateFlow(prefs.getBoolean("focus_completion_notification", true))
    val focusCompletionNotificationEnabled: StateFlow<Boolean> = _focusCompletionNotificationEnabled.asStateFlow()

    private val _focusCustomWorkMinutes = MutableStateFlow(prefs.getInt("focus_custom_work_mins", 25))
    val focusCustomWorkMinutes: StateFlow<Int> = _focusCustomWorkMinutes.asStateFlow()

    private val _focusCustomShortBreakMinutes = MutableStateFlow(prefs.getInt("focus_custom_short_break_mins", 5))
    val focusCustomShortBreakMinutes: StateFlow<Int> = _focusCustomShortBreakMinutes.asStateFlow()

    private val _focusCustomLongBreakMinutes = MutableStateFlow(prefs.getInt("focus_custom_long_break_mins", 15))
    val focusCustomLongBreakMinutes: StateFlow<Int> = _focusCustomLongBreakMinutes.asStateFlow()

    private val _focusCustomCyclesBeforeLongBreak = MutableStateFlow(prefs.getInt("focus_custom_cycles", 4))
    val focusCustomCyclesBeforeLongBreak: StateFlow<Int> = _focusCustomCyclesBeforeLongBreak.asStateFlow()

    private val _focusDailyMinutesGoal = MutableStateFlow(prefs.getInt("focus_daily_minutes_goal", 100))
    val focusDailyMinutesGoal: StateFlow<Int> = _focusDailyMinutesGoal.asStateFlow()

    private val _focusDailySessionsGoal = MutableStateFlow(prefs.getInt("focus_daily_sessions_goal", 4))
    val focusDailySessionsGoal: StateFlow<Int> = _focusDailySessionsGoal.asStateFlow()

    // Stored focus log history (JSON array) for today's stats & history
    private val _focusHistoryJson = MutableStateFlow(prefs.getString("focus_history_json", "[]") ?: "[]")
    val focusHistoryJson: StateFlow<String> = _focusHistoryJson.asStateFlow()

    fun addXp(amount: Long) {
        val updated = (_totalXp.value + amount).coerceAtLeast(0L)
        prefs.edit().putLong("user_total_xp", updated).apply()
        _totalXp.value = updated
    }

    fun useStreakShield(): Boolean {
        if (_streakShields.value > 0) {
            val remaining = _streakShields.value - 1
            val used = _streakShieldsUsed.value + 1
            prefs.edit()
                .putInt("streak_shields_count", remaining)
                .putInt("streak_shields_used", used)
                .apply()
            _streakShields.value = remaining
            _streakShieldsUsed.value = used
            return true
        }
        return false
    }

    fun addStreakShield(count: Int = 1) {
        val updated = _streakShields.value + count
        prefs.edit().putInt("streak_shields_count", updated).apply()
        _streakShields.value = updated
    }

    fun setHighContrastEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("high_contrast_enabled", enabled).apply()
        _highContrastEnabled.value = enabled
    }

    fun setLargeTextScaleEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("large_text_scale_enabled", enabled).apply()
        _largeTextScaleEnabled.value = enabled
    }

    fun setColorblindMode(mode: String) {
        prefs.edit().putString("colorblind_mode", mode).apply()
        _colorblindMode.value = mode
    }

    fun setAutoArchiveDays(days: Int) {
        prefs.edit().putInt("auto_archive_days", days).apply()
        _autoArchiveDays.value = days
    }

    fun setHideUnusedCategories(hide: Boolean) {
        prefs.edit().putBoolean("hide_unused_categories", hide).apply()
        _hideUnusedCategories.value = hide
    }

    fun setSmartNotificationsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("smart_notifications_enabled", enabled).apply()
        _smartNotificationsEnabled.value = enabled
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
        _onboardingCompleted.value = completed
    }

    fun setTravelModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("travel_mode_enabled", enabled).apply()
        _travelModeEnabled.value = enabled
    }

    fun setBackupPassword(password: String?) {
        if (password.isNullOrBlank()) {
            prefs.edit().remove("backup_password_hash").putBoolean("has_backup_password", false).apply()
            _hasBackupPassword.value = false
        } else {
            // Store simple SHA-256 hash
            val md = java.security.MessageDigest.getInstance("SHA-256")
            val hash = md.digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
            prefs.edit().putString("backup_password_hash", hash).putBoolean("has_backup_password", true).apply()
            _hasBackupPassword.value = true
        }
    }

    fun verifyBackupPassword(password: String): Boolean {
        val storedHash = prefs.getString("backup_password_hash", null) ?: return true
        val md = java.security.MessageDigest.getInstance("SHA-256")
        val inputHash = md.digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
        return storedHash == inputHash
    }

    fun setGoogleFitSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("google_fit_sync_enabled", enabled).apply()
        _googleFitSyncEnabled.value = enabled
    }

    fun setQuietHoursEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("quiet_hours_enabled", enabled).apply()
        _quietHoursEnabled.value = enabled
    }

    fun setQuietHoursTimes(start: String, end: String) {
        prefs.edit().putString("quiet_hours_start", start).putString("quiet_hours_end", end).apply()
        _quietHoursStart.value = start
        _quietHoursEnd.value = end
    }

    fun isQuietHoursNow(): Boolean {
        if (!_quietHoursEnabled.value) return false
        return try {
            val now = java.time.LocalTime.now()
            val start = java.time.LocalTime.parse(_quietHoursStart.value)
            val end = java.time.LocalTime.parse(_quietHoursEnd.value)
            if (start.isBefore(end)) {
                now.isAfter(start) && now.isBefore(end)
            } else {
                now.isAfter(start) || now.isBefore(end)
            }
        } catch (_: Exception) {
            false
        }
    }

    fun setColorPalette(palette: String) {
        prefs.edit().putString("color_palette", palette).apply()
        _colorPalette.value = palette
    }

    fun setAppLanguage(lang: String) {
        val clean = if (lang == "ar") "ar" else "en"
        prefs.edit().putString("app_language", clean).apply()
        _appLanguage.value = clean
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

    // Pomodoro Setters
    fun setPartialCreditEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("focus_partial_credit_enabled", enabled).apply()
        _partialCreditEnabled.value = enabled
    }

    fun setFocusLastDurationMinutes(minutes: Int) {
        val m = minutes.coerceIn(5, 120)
        prefs.edit().putInt("focus_last_duration_minutes", m).apply()
        _focusLastDurationMinutes.value = m
    }

    fun setFocusSessionMode(mode: String) {
        prefs.edit().putString("focus_session_mode", mode).apply()
        _focusSessionMode.value = mode
    }

    fun setFocusAutoStartType(type: String) {
        prefs.edit().putString("focus_auto_start_type", type).apply()
        _focusAutoStartType.value = type
    }

    fun setFocusOneMinuteWarningEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("focus_one_min_warning", enabled).apply()
        _focusOneMinuteWarningEnabled.value = enabled
    }

    fun setFocusCompletionNotificationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("focus_completion_notification", enabled).apply()
        _focusCompletionNotificationEnabled.value = enabled
    }

    fun setFocusCustomWorkMinutes(minutes: Int) {
        val m = minutes.coerceIn(5, 120)
        prefs.edit().putInt("focus_custom_work_mins", m).apply()
        _focusCustomWorkMinutes.value = m
    }

    fun setFocusCustomShortBreakMinutes(minutes: Int) {
        val m = minutes.coerceIn(1, 30)
        prefs.edit().putInt("focus_custom_short_break_mins", m).apply()
        _focusCustomShortBreakMinutes.value = m
    }

    fun setFocusCustomLongBreakMinutes(minutes: Int) {
        val m = minutes.coerceIn(5, 60)
        prefs.edit().putInt("focus_custom_long_break_mins", m).apply()
        _focusCustomLongBreakMinutes.value = m
    }

    fun setFocusCustomCyclesBeforeLongBreak(cycles: Int) {
        val c = cycles.coerceIn(2, 8)
        prefs.edit().putInt("focus_custom_cycles", c).apply()
        _focusCustomCyclesBeforeLongBreak.value = c
    }

    fun setFocusDailyGoals(sessions: Int, minutes: Int) {
        prefs.edit()
            .putInt("focus_daily_sessions_goal", sessions.coerceAtLeast(1))
            .putInt("focus_daily_minutes_goal", minutes.coerceAtLeast(10))
            .apply()
        _focusDailySessionsGoal.value = sessions.coerceAtLeast(1)
        _focusDailyMinutesGoal.value = minutes.coerceAtLeast(10)
    }

    fun addFocusSessionRecord(
        taskId: Long?,
        taskTitle: String?,
        targetMinutes: Int,
        elapsedMinutes: Int,
        status: String
    ) {
        try {
            val jsonArray = org.json.JSONArray(_focusHistoryJson.value)
            val obj = org.json.JSONObject().apply {
                put("timestamp", System.currentTimeMillis())
                put("taskId", taskId ?: -1L)
                put("taskTitle", taskTitle ?: "")
                put("targetMinutes", targetMinutes)
                put("elapsedMinutes", elapsedMinutes)
                put("status", status)
                put("date", java.time.LocalDate.now().toString())
            }
            jsonArray.put(obj)
            val updatedJson = jsonArray.toString()
            prefs.edit().putString("focus_history_json", updatedJson).apply()
            _focusHistoryJson.value = updatedJson
        } catch (_: Exception) {}
    }

    fun resetAllGamificationAndHistory() {
        prefs.edit()
            .putLong("user_total_xp", 0L)
            .putInt("streak_shields_count", 2)
            .putInt("streak_shields_used", 0)
            .putString("focus_history_json", "[]")
            .putLong("last_local_backup_time", 0L)
            .putLong("drive_last_synced_time", 0L)
            .apply()

        _totalXp.value = 0L
        _streakShields.value = 2
        _streakShieldsUsed.value = 0
        _focusHistoryJson.value = "[]"
        _lastLocalBackupTime.value = 0L
        _driveLastSyncedTime.value = 0L
    }
}
