package com.abubakr.taskstreak.data.model

data class UserLevel(
    val level: Int,
    val title: String,
    val titleAr: String,
    val badgeIcon: String,
    val xpInCurrentLevel: Int,
    val xpRequiredForNextLevel: Int = 100,
    val progress: Float
) {
    fun localizedTitle(isArabic: Boolean): String = if (isArabic) titleAr else title
}

data class Quest(
    val id: String,
    val title: String,
    val titleAr: String,
    val description: String,
    val descriptionAr: String,
    val xpReward: Int,
    val progress: Int,
    val target: Int,
    val isCompleted: Boolean = progress >= target
) {
    fun localizedTitle(isArabic: Boolean): String = if (isArabic) titleAr else title
    fun localizedDescription(isArabic: Boolean): String = if (isArabic) descriptionAr else description
}

object GamificationCalculator {
    fun calculateLevel(totalXp: Long): UserLevel {
        val level = ((totalXp / 100) + 1).toInt()
        val xpInLevel = (totalXp % 100).toInt()
        val progress = (xpInLevel.toFloat() / 100f).coerceIn(0f, 1f)

        val (titleEn, titleAr) = when (level) {
            in 1..3 -> "Bronze Apprentice" to "مبتدئ برونزي"
            in 4..7 -> "Bronze Champion" to "بطل برونزي"
            in 8..12 -> "Silver Habit Striker" to "محترف فضي"
            in 13..18 -> "Silver Momentum Master" to "سيد الزخم الفضي"
            in 19..25 -> "Gold Streak Legend" to "أسطورة السلاسل الذهبي"
            in 26..35 -> "Platinum Phoenix" to "فينيق بلاتيني"
            else -> "Diamond Eternal 🔥" to "ألماسي أبدي 🔥"
        }

        val badgeIcon = when (level) {
            in 1..7 -> "🥉"
            in 8..18 -> "🥈"
            in 19..25 -> "🥇"
            in 26..35 -> "💎"
            else -> "👑"
        }

        return UserLevel(
            level = level,
            title = titleEn,
            titleAr = titleAr,
            badgeIcon = badgeIcon,
            xpInCurrentLevel = xpInLevel,
            progress = progress
        )
    }

    fun getDailyQuests(completedTodayCount: Int, pomodoroSessions: Int, currentBestStreak: Int): List<Quest> {
        return listOf(
            Quest(
                id = "quest_daily_3",
                title = "Daily Momentum",
                titleAr = "زخم اليوم",
                description = "Complete 3 habits or tasks today",
                descriptionAr = "أكمل 3 عادات أو مهام اليوم",
                xpReward = 50,
                progress = completedTodayCount.coerceAtMost(3),
                target = 3
            ),
            Quest(
                id = "quest_pomodoro",
                title = "Deep Focus",
                titleAr = "تركيز عميق",
                description = "Complete at least 1 Pomodoro focus block",
                descriptionAr = "أكمل جلسة تركيز بومودورو واحدة على الأقل",
                xpReward = 40,
                progress = pomodoroSessions.coerceAtMost(1),
                target = 1
            ),
            Quest(
                id = "quest_streak_keeper",
                title = "Fire Keeper",
                titleAr = "حامي الشعلة",
                description = "Reach or maintain a 5+ day streak on any habit",
                descriptionAr = "حافظ على سلسلة 5 أيام أو أكثر في أي عادة",
                xpReward = 75,
                progress = currentBestStreak.coerceAtMost(5),
                target = 5
            )
        )
    }
}
