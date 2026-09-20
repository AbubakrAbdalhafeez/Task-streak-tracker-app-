package com.abubakr.taskstreak.util

import android.content.Context
import android.content.Intent
import com.abubakr.taskstreak.data.model.TaskEntity

data class FriendStreakBadge(
    val friendName: String,
    val taskTitle: String,
    val streakDays: Int,
    val dateVerified: String
)

object SocialShareHelper {
    /**
     * Generates a shareable streak text & digital passport
     */
    fun shareStreakAchievement(context: Context, task: TaskEntity, streakDays: Int) {
        val shareText = """
            🔥 Task Streak Milestone!
            I've maintained a $streakDays-day active streak for "${task.title}"!
            
            Consistency is key! Track your streaks offline with Task Streak Tracker 🔥
            Passport Code: STREAK#${task.id}#${streakDays}D#${DateUtils.todayString()}
        """.trimIndent()

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Streak Achievement")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun shareHabitStreak(context: Context, task: TaskEntity, streakDays: Int) {
        shareStreakAchievement(context, task, streakDays)
    }

    fun shareAchievement(context: Context, title: String, description: String) {
        val shareText = """
            🏆 Achievement Unlocked: $title!
            $description
            
            Building healthy daily habits with Task Streak Tracker! 🔥
        """.trimIndent()

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Achievement")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    /**
     * Parses a shared friend streak passport
     */
    fun parseFriendPassport(code: String): FriendStreakBadge? {
        return try {
            val parts = code.trim().split("#")
            if (parts.size >= 4 && parts[0] == "STREAK") {
                val days = parts[2].replace("D", "").toIntOrNull() ?: 1
                FriendStreakBadge(
                    friendName = "Friend",
                    taskTitle = "Habit #${parts[1]}",
                    streakDays = days,
                    dateVerified = parts[3]
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
