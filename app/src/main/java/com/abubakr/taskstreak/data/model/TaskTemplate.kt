package com.abubakr.taskstreak.data.model

data class TaskTemplate(
    val id: String,
    val title: String,
    val titleAr: String,
    val category: String,
    val categoryColorHex: String,
    val recurrenceType: String = RecurrenceType.DAILY.name,
    val customDaysOfWeek: String = "1,2,3,4,5,6,7",
    val reminderTime: String? = null,
    val note: String? = null,
    val noteAr: String? = null,
    val isHabit: Boolean = true,
    val defaultSubtasks: List<String> = emptyList()
) {
    fun localizedTitle(isArabic: Boolean): String = if (isArabic) titleAr else title
    fun localizedNote(isArabic: Boolean): String? = if (isArabic && noteAr != null) noteAr else note

    companion object {
        fun getStarterTemplates(): List<TaskTemplate> = listOf(
            TaskTemplate(
                id = "tpl_water",
                title = "Drink 2L Water",
                titleAr = "شرب لترين ماء",
                category = "Fitness",
                categoryColorHex = "#10B981",
                recurrenceType = RecurrenceType.DAILY.name,
                reminderTime = "09:00",
                note = "Keep a water bottle beside your desk",
                noteAr = "احتفظ بزجاجة ماء بجوار مكتبك",
                defaultSubtasks = listOf("Morning glass", "Afternoon refill", "Evening glass")
            ),
            TaskTemplate(
                id = "tpl_workout",
                title = "Morning Workout",
                titleAr = "تمرين صباحي",
                category = "Fitness",
                categoryColorHex = "#10B981",
                recurrenceType = RecurrenceType.CUSTOM.name,
                customDaysOfWeek = "1,2,3,4,5",
                reminderTime = "07:00",
                note = "Stretching + 20 mins cardio or strength",
                noteAr = "إطالات وتمارين كارديو أو قوة لمدة 20 دقيقة"
            ),
            TaskTemplate(
                id = "tpl_reading",
                title = "Read 20 Pages",
                titleAr = "قراءة 20 صفحة",
                category = "Study",
                categoryColorHex = "#3B82F6",
                recurrenceType = RecurrenceType.DAILY.name,
                reminderTime = "21:00",
                note = "Read before bedtime",
                noteAr = "القراءة الهادئة قبل النوم"
            ),
            TaskTemplate(
                id = "tpl_english",
                title = "English Practice",
                titleAr = "تعلم الإنجليزية",
                category = "English",
                categoryColorHex = "#8B5CF6",
                recurrenceType = RecurrenceType.DAILY.name,
                reminderTime = "18:30",
                note = "Practice 10 new vocabulary words",
                noteAr = "تعلم وممارسة 10 مفردات جديدة",
                defaultSubtasks = listOf("10 flashcards", "Listen to a podcast")
            ),
            TaskTemplate(
                id = "tpl_journal",
                title = "Evening Journaling",
                titleAr = "تدوين يومي",
                category = "Personal",
                categoryColorHex = "#EC4899",
                recurrenceType = RecurrenceType.DAILY.name,
                reminderTime = "22:00",
                note = "Write 3 things you are grateful for today",
                noteAr = "اكتب 3 نعم تشكر الله عليها اليوم"
            ),
            TaskTemplate(
                id = "tpl_deep_work",
                title = "Deep Focus Block",
                titleAr = "جلسة تركيز عميق",
                category = "Work",
                categoryColorHex = "#F59E0B",
                recurrenceType = RecurrenceType.CUSTOM.name,
                customDaysOfWeek = "1,2,3,4,5",
                reminderTime = "10:00",
                note = "Turn off phone notifications and focus",
                noteAr = "أوقف إشعارات الهاتف وابدأ العمل المركز"
            )
        )
    }
}
