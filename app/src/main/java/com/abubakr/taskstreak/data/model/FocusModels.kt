package com.abubakr.taskstreak.data.model

enum class FocusSessionMode(val titleEn: String, val titleAr: String) {
    CLASSIC("Classic Pomodoro", "النمط الكلاسيكي (25/5/15)"),
    DEEP_WORK("Deep Work", "العمل العميق (90/15)"),
    CUSTOM("Custom Mode", "النمط المخصص")
}

enum class FocusAutoStartType(val titleEn: String, val titleAr: String) {
    MANUAL("Manual (No auto-start)", "يدوي بالكامل"),
    BREAK_ONLY("Auto-start Break only", "الاستراحة تبدأ تلقائياً"),
    FULL_AUTO("Full Auto-start", "تلقائي كامل (جلسات واستراحات)"),
    CONTINUOUS_NO_BREAK("Continuous (No breaks)", "جلسات متواصلة بدون استراحة")
}

enum class FocusSessionStatus(val labelEn: String, val labelAr: String) {
    COMPLETED("Completed", "مكتملة"),
    PARTIAL("Partial Credit", "جزئية"),
    FAILED("Abandoned", "فاشلة")
}

data class FocusRecord(
    val timestamp: Long = System.currentTimeMillis(),
    val taskId: Long? = null,
    val taskTitle: String? = null,
    val targetMinutes: Int,
    val elapsedMinutes: Int,
    val status: FocusSessionStatus
)
