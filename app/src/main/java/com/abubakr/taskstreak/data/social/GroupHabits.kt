package com.abubakr.taskstreak.data.social

data class SharedGroupHabit(
    val id: String,
    val title: String,
    val titleAr: String,
    val description: String,
    val descriptionAr: String,
    val category: String,
    val categoryAr: String,
    val teamName: String,
    val teamNameAr: String,
    val groupStreakDays: Int,
    val members: List<GroupMember>,
    val chatMessages: List<GroupChatMessage>
) {
    fun localizedTitle(isArabic: Boolean): String = if (isArabic) titleAr else title
    fun localizedDescription(isArabic: Boolean): String = if (isArabic) descriptionAr else description
    fun localizedCategory(isArabic: Boolean): String = if (isArabic) categoryAr else category
    fun localizedTeamName(isArabic: Boolean): String = if (isArabic) teamNameAr else teamName
}

data class GroupMember(
    val id: String,
    val name: String,
    val nameAr: String,
    val avatarInitial: String,
    val completedToday: Boolean,
    val personalStreak: Int
) {
    fun localizedName(isArabic: Boolean): String = if (isArabic) nameAr else name
}

data class GroupChatMessage(
    val id: String,
    val senderName: String,
    val senderNameAr: String,
    val message: String,
    val messageAr: String,
    val timestamp: String,
    val timestampAr: String,
    val isCelebration: Boolean = false
) {
    fun localizedSender(isArabic: Boolean): String = if (isArabic) senderNameAr else senderName
    fun localizedMessage(isArabic: Boolean): String = if (isArabic) messageAr else message
    fun localizedTime(isArabic: Boolean): String = if (isArabic) timestampAr else timestamp
}

object GroupHabitRepository {
    private val groupHabits = mutableListOf(
        SharedGroupHabit(
            id = "group_1",
            title = "Morning 5KM Run",
            titleAr = "مشي أو ركض 5 كم صباحاً",
            description = "Daily morning run or brisk walk before 9:00 AM.",
            descriptionAr = "الجري الصباحي أو المشي السريع يومياً قبل الساعة التاسعة صباحاً.",
            category = "Fitness",
            categoryAr = "اللياقة البدنية",
            teamName = "Dawn Runners 🌅",
            teamNameAr = "عدائو الفجر 🌅",
            groupStreakDays = 14,
            members = listOf(
                GroupMember("m1", "You (Abubakr)", "أنت (أبو بكر)", "A", completedToday = true, personalStreak = 14),
                GroupMember("m2", "Sami K.", "سامي ك.", "S", completedToday = true, personalStreak = 21),
                GroupMember("m3", "Omar D.", "عمر د.", "O", completedToday = false, personalStreak = 12),
                GroupMember("m4", "Nour M.", "نور م.", "N", completedToday = true, personalStreak = 9)
            ),
            chatMessages = listOf(
                GroupChatMessage("c1", "Sami K.", "سامي ك.", "Finished 5.2 km through the park! Cold morning 🏃", "أنجزت 5.2 كم في الحديقة! صباح منعش وبارد 🏃", "07:15 AM", "07:15 ص"),
                GroupChatMessage("c2", "You", "أنت", "Checked off! Kept up the pace today 🚀", "تم الإنجاز! حافظت على سرعتي اليوم 🚀", "07:45 AM", "07:45 ص", isCelebration = true),
                GroupChatMessage("c3", "Nour M.", "نور م.", "Done! Let's keep our 14-day team streak alive 🔥", "تم! لنحافظ على سلسلة الـ 14 يوماً للفريق مشتعلة 🔥", "08:10 AM", "08:10 ص")
            )
        ),
        SharedGroupHabit(
            id = "group_2",
            title = "Deep Work Reading Club",
            titleAr = "نادي القراءة والعمل العميق",
            description = "Read at least 20 pages of non-fiction or educational literature daily.",
            descriptionAr = "قراءة ما لا يقل عن 20 صفحة من الكتب المفيدة أو الأدب التعليمي يومياً.",
            category = "Learning",
            categoryAr = "التعلم والتطوير",
            teamName = "Mindset Builders 📚",
            teamNameAr = "بناة العقلية 📚",
            groupStreakDays = 28,
            members = listOf(
                GroupMember("m1", "You (Abubakr)", "أنت (أبو بكر)", "A", completedToday = true, personalStreak = 28),
                GroupMember("m5", "Tariq A.", "طارق ع.", "T", completedToday = true, personalStreak = 30),
                GroupMember("m6", "Zaid H.", "زيد ح.", "Z", completedToday = true, personalStreak = 28)
            ),
            chatMessages = listOf(
                GroupChatMessage("c4", "Tariq A.", "طارق ع.", "Chapter 4 of Atomic Habits was incredible today.", "الفصل الرابع من كتاب العادات الذرية كان ملهماً جداً اليوم.", "06:30 AM", "06:30 ص"),
                GroupChatMessage("c5", "You", "أنت", "Completed 25 pages! 📚", "أتممت 25 صفحة اليوم! 📚", "08:00 AM", "08:00 ص", isCelebration = true)
            )
        )
    )

    fun getGroupHabits(): List<SharedGroupHabit> = groupHabits

    fun addChatMessage(groupId: String, messageText: String, isArabic: Boolean = false): SharedGroupHabit? {
        val idx = groupHabits.indexOfFirst { it.id == groupId }
        if (idx != -1) {
            val habit = groupHabits[idx]
            val newMsg = GroupChatMessage(
                id = "c_${System.currentTimeMillis()}",
                senderName = "You",
                senderNameAr = "أنت",
                message = messageText,
                messageAr = messageText,
                timestamp = "Just now",
                timestampAr = "الآن",
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
