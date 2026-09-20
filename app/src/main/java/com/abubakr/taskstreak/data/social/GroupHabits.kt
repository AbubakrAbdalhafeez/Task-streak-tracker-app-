package com.abubakr.taskstreak.data.social

data class SharedGroupHabit(
    val id: String,
    val title: String,
    val titleAr: String,
    val description: String,
    val category: String,
    val teamName: String,
    val groupStreakDays: Int,
    val members: List<GroupMember>,
    val chatMessages: List<GroupChatMessage>
) {
    fun localizedTitle(isArabic: Boolean): String = if (isArabic) titleAr else title
}

data class GroupMember(
    val id: String,
    val name: String,
    val avatarInitial: String,
    val completedToday: Boolean,
    val personalStreak: Int
)

data class GroupChatMessage(
    val id: String,
    val senderName: String,
    val message: String,
    val timestamp: String,
    val isCelebration: Boolean = false
)

object GroupHabitRepository {
    private val groupHabits = mutableListOf(
        SharedGroupHabit(
            id = "group_1",
            title = "Morning 5KM Run",
            titleAr = "مشي صباحي 5 كم",
            description = "Daily morning run or brisk walk before 9:00 AM.",
            category = "Fitness",
            teamName = "Dawn Runners 🌅",
            groupStreakDays = 14,
            members = listOf(
                GroupMember("m1", "You (Abubakr)", "A", completedToday = true, personalStreak = 14),
                GroupMember("m2", "Sami K.", "S", completedToday = true, personalStreak = 21),
                GroupMember("m3", "Omar D.", "O", completedToday = false, personalStreak = 12),
                GroupMember("m4", "Nour M.", "N", completedToday = true, personalStreak = 9)
            ),
            chatMessages = listOf(
                GroupChatMessage("c1", "Sami K.", "Finished 5.2 km through the park! Cold morning 🏃", "07:15 AM"),
                GroupChatMessage("c2", "You", "Checked off! Kept up the pace today 🚀", "07:45 AM", isCelebration = true),
                GroupChatMessage("c3", "Nour M.", "Done! Let's keep our 14-day team streak alive 🔥", "08:10 AM")
            )
        ),
        SharedGroupHabit(
            id = "group_2",
            title = "Deep Work Reading Club",
            titleAr = "قراءة 20 صفحة يومياً",
            description = "Read at least 20 pages of non-fiction or educational literature daily.",
            category = "Learning",
            teamName = "Mindset Builders 📚",
            groupStreakDays = 28,
            members = listOf(
                GroupMember("m1", "You (Abubakr)", "A", completedToday = true, personalStreak = 28),
                GroupMember("m5", "Tariq A.", "T", completedToday = true, personalStreak = 30),
                GroupMember("m6", "Zaid H.", "Z", completedToday = true, personalStreak = 28)
            ),
            chatMessages = listOf(
                GroupChatMessage("c4", "Tariq A.", "Chapter 4 of Atomic Habits was incredible today.", "06:30 AM"),
                GroupChatMessage("c5", "You", "Completed 25 pages! 📚", "08:00 AM", isCelebration = true)
            )
        )
    )

    fun getGroupHabits(): List<SharedGroupHabit> = groupHabits

    fun addChatMessage(groupId: String, messageText: String): SharedGroupHabit? {
        val idx = groupHabits.indexOfFirst { it.id == groupId }
        if (idx != -1) {
            val habit = groupHabits[idx]
            val newMsg = GroupChatMessage(
                id = "c_${System.currentTimeMillis()}",
                senderName = "You",
                message = messageText,
                timestamp = "Just now",
                isCelebration = messageText.contains("done", ignoreCase = true) || messageText.contains("تم", ignoreCase = true)
            )
            val updated = habit.copy(chatMessages = habit.chatMessages + newMsg)
            groupHabits[idx] = updated
            return updated
        }
        return null
    }

    fun toggleMemberCompletion(groupId: String): SharedGroupHabit? {
        val idx = groupHabits.indexOfFirst { it.id == groupId }
        if (idx != -1) {
            val habit = groupHabits[idx]
            val updatedMembers = habit.members.map { member ->
                if (member.id == "m1") {
                    member.copy(
                        completedToday = !member.completedToday,
                        personalStreak = if (!member.completedToday) member.personalStreak + 1 else member.personalStreak - 1
                    )
                } else member
            }
            val allDone = updatedMembers.all { it.completedToday }
            val newStreak = if (allDone) habit.groupStreakDays + 1 else habit.groupStreakDays
            val updated = habit.copy(members = updatedMembers, groupStreakDays = newStreak)
            groupHabits[idx] = updated
            return updated
        }
        return null
    }
}
