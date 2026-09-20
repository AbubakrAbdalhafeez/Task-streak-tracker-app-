package com.abubakr.taskstreak.data.model

import com.abubakr.taskstreak.util.OverallStats
import com.abubakr.taskstreak.util.TaskStreakStats

data class Achievement(
    val id: String,
    val title: String,
    val titleAr: String,
    val description: String,
    val descriptionAr: String,
    val iconEmoji: String,
    val isUnlocked: Boolean,
    val progress: Float, // 0.0 to 1.0
    val progressText: String
) {
    fun localizedTitle(isArabic: Boolean): String = if (isArabic) titleAr else title
    fun localizedDescription(isArabic: Boolean): String = if (isArabic) descriptionAr else description
}

object AchievementEvaluator {
    fun evaluateAchievements(
        overallStats: OverallStats,
        taskStats: Map<Long, TaskStreakStats>
    ): List<Achievement> {
        val totalCompletions = overallStats.totalCompletionsAllTime
        val bestStreak = overallStats.bestOverallStreak

        // 1. First Step
        val firstStepUnlocked = totalCompletions >= 1
        val firstStep = Achievement(
            id = "first_step",
            title = "First Step",
            titleAr = "الخطوة الأولى",
            description = "Complete your first task ever",
            descriptionAr = "أكمل أول مهمة لك على الإطلاق",
            iconEmoji = "🌱",
            isUnlocked = firstStepUnlocked,
            progress = if (firstStepUnlocked) 1f else 0f,
            progressText = if (firstStepUnlocked) "1/1 Completed" else "0/1"
        )

        // 2. 7 in a Row
        val sevenInRowUnlocked = bestStreak >= 7
        val sevenInRow = Achievement(
            id = "seven_in_a_row",
            title = "7 in a Row",
            titleAr = "أسبوع ناري",
            description = "Reach a 7-day streak on any task",
            descriptionAr = "حقق سلسلة إنجاز لمدة 7 أيام في أي عادة",
            iconEmoji = "🔥",
            isUnlocked = sevenInRowUnlocked,
            progress = (bestStreak.toFloat() / 7f).coerceIn(0f, 1f),
            progressText = "$bestStreak/7 Days"
        )

        // 3. 30 in a Row
        val thirtyInRowUnlocked = bestStreak >= 30
        val thirtyInRow = Achievement(
            id = "thirty_in_a_row",
            title = "30 in a Row",
            titleAr = "شهر فولاذي",
            description = "Reach a 30-day streak on any task",
            descriptionAr = "حقق سلسلة إنجاز لمدة 30 يوماً متواصلاً",
            iconEmoji = "⚡",
            isUnlocked = thirtyInRowUnlocked,
            progress = (bestStreak.toFloat() / 30f).coerceIn(0f, 1f),
            progressText = "$bestStreak/30 Days"
        )

        // 4. High Achiever (80% completion on a task with at least 10 scheduled days)
        val highAchieverTask = taskStats.values.firstOrNull { it.totalScheduledDays >= 10 && it.completionRate >= 80f }
        val maxRateOnQualified = taskStats.values
            .filter { it.totalScheduledDays >= 5 }
            .maxOfOrNull { it.completionRate } ?: 0f

        val highAchieverUnlocked = highAchieverTask != null
        val highAchiever = Achievement(
            id = "high_achiever",
            title = "High Achiever",
            titleAr = "صاحب الهمة",
            description = "Reach 80%+ completion rate on an established task (10+ scheduled days)",
            descriptionAr = "حقق نسبة إنجاز 80%+ في مهمة مجدولة لمدة 10 أيام على الأقل",
            iconEmoji = "🎯",
            isUnlocked = highAchieverUnlocked,
            progress = (maxRateOnQualified / 80f).coerceIn(0f, 1f),
            progressText = "${maxRateOnQualified.toInt()}% / 80%"
        )

        // 5. Centurion
        val centurionUnlocked = totalCompletions >= 100
        val centurion = Achievement(
            id = "centurion",
            title = "Centurion",
            titleAr = "نادي المائة",
            description = "Reach 100 total task completions all time",
            descriptionAr = "أنجز 100 مهمة في المجمل منذ انطلاقتك",
            iconEmoji = "🏆",
            isUnlocked = centurionUnlocked,
            progress = (totalCompletions.toFloat() / 100f).coerceIn(0f, 1f),
            progressText = "$totalCompletions/100"
        )

        // 6. Perfect Day
        val perfectDayUnlocked = overallStats.totalScheduledTodayCount >= 3 &&
                overallStats.completedTodayCount == overallStats.totalScheduledTodayCount
        val perfectDay = Achievement(
            id = "perfect_day",
            title = "Perfect Day",
            titleAr = "يوم مثالي",
            description = "Finish all scheduled tasks for today (min 3 tasks)",
            descriptionAr = "أنهِ جميع المهام المجدولة لليوم (3 مهام على الأقل)",
            iconEmoji = "✨",
            isUnlocked = perfectDayUnlocked,
            progress = if (overallStats.totalScheduledTodayCount > 0) {
                (overallStats.completedTodayCount.toFloat() / overallStats.totalScheduledTodayCount.toFloat()).coerceIn(0f, 1f)
            } else 0f,
            progressText = "${overallStats.completedTodayCount}/${overallStats.totalScheduledTodayCount} Today"
        )

        return listOf(firstStep, sevenInRow, thirtyInRow, highAchiever, centurion, perfectDay)
    }
}
