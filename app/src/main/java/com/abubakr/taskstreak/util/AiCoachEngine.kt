package com.abubakr.taskstreak.util

import com.abubakr.taskstreak.data.model.TaskEntity
import java.time.DayOfWeek
import java.time.LocalDate

data class AiCoachingAdvice(
    val title: String,
    val titleAr: String,
    val tip: String,
    val tipAr: String,
    val actionSuggestion: String,
    val actionSuggestionAr: String,
    val confidenceScore: Float
) {
    fun localizedTitle(isArabic: Boolean): String = if (isArabic) titleAr else title
    fun localizedTip(isArabic: Boolean): String = if (isArabic) tipAr else tip
    fun localizedActionSuggestion(isArabic: Boolean): String = if (isArabic) actionSuggestionAr else actionSuggestion
}

object AiCoachEngine {

    fun generateCoachingInsights(
        tasks: List<TaskEntity>,
        taskStats: Map<Long, TaskStreakStats>,
        peakHours: String,
        bestDay: String
    ): List<AiCoachingAdvice> {
        val insights = mutableListOf<AiCoachingAdvice>()

        // 1. Analyze Task Skip Risk (tasks with low consistency or currently broken streak)
        val atRiskTasks = tasks.filter { task ->
            val stat = taskStats[task.id]
            stat != null && stat.completionRate < 0.5f && stat.totalCompletions > 0
        }

        if (atRiskTasks.isNotEmpty()) {
            val taskName = atRiskTasks.first().title
            insights.add(
                AiCoachingAdvice(
                    title = "Habit At Risk: $taskName",
                    titleAr = "عادة معرضة للانقطاع: $taskName",
                    tip = "Your completion rate for '$taskName' has dipped under 50%. Try scaling it down to a 2-minute micro-habit to rebuild momentum!",
                    tipAr = "نسبة التزامك بـ '$taskName' انخفضت. جرّب تقليص مدتها إلى دقيقتين فقط لإعادة بناء الزخم.",
                    actionSuggestion = "Reduce friction & set reminder for $peakHours",
                    actionSuggestionAr = "قلل صعوبة البداية واضبط التذكير على $peakHours",
                    confidenceScore = 0.92f
                )
            )
        }

        // 2. Best day of week leverage
        if (bestDay.isNotBlank()) {
            val bestDayAr = when (bestDay.lowercase()) {
                "sunday" -> "الأحد"
                "monday" -> "الاثنين"
                "tuesday" -> "الثلاثاء"
                "wednesday" -> "الأربعاء"
                "thursday" -> "الخميس"
                "friday" -> "الجمعة"
                "saturday" -> "السبت"
                else -> bestDay
            }
            insights.add(
                AiCoachingAdvice(
                    title = "Power Day Opportunity: $bestDay",
                    titleAr = "يوم الذروة: $bestDayAr",
                    tip = "You consistently finish the most habits on $bestDay. Schedule demanding or challenging new routines on this day for maximum success.",
                    tipAr = "أعلى نسبة إنجاز لديك تكون في يوم $bestDayAr. استغل هذا اليوم للعادات الأكثر تحدياً.",
                    actionSuggestion = "Pair new habits with $bestDay routine",
                    actionSuggestionAr = "اربط العادات الجديدة بروتين يوم $bestDayAr",
                    confidenceScore = 0.88f
                )
            )
        }

        // 3. Optimal Reminder Timing Advice
        if (peakHours.isNotBlank() && peakHours != "None yet") {
            insights.add(
                AiCoachingAdvice(
                    title = "Optimal Focus Window: $peakHours",
                    titleAr = "نافذة التركيز المثالية: $peakHours",
                    tip = "Historical analytics show your brain is primed for task execution around $peakHours. Sync your reminder alarms to this window.",
                    tipAr = "البيانات تشير إلى أن قمة إنتاجيتك تكون خلال $peakHours. اضبط تذكيراتك في هذا التوقيت.",
                    actionSuggestion = "Align reminders with $peakHours",
                    actionSuggestionAr = "اضبط التنبيهات على $peakHours",
                    confidenceScore = 0.95f
                )
            )
        }

        // 4. Stacking recommendation
        val highStreakTasks = tasks.filter { task ->
            (taskStats[task.id]?.currentStreak ?: 0) >= 7
        }
        if (highStreakTasks.isNotEmpty() && tasks.size > 1) {
            val anchor = highStreakTasks.first().title
            insights.add(
                AiCoachingAdvice(
                    title = "Habit Stacking Recommendation",
                    titleAr = "اقتراح تكديس العادات",
                    tip = "Anchor new habits right after '$anchor', which has an unbroken streak! Formula: 'After I finish $anchor, I will immediately do my new habit.'",
                    tipAr = "اربط عاداتك الجديدة مباشرة بعد '$anchor' لأن سلسلتها قوية ومستقرة.",
                    actionSuggestion = "Create Habit Chain with $anchor",
                    actionSuggestionAr = "أنشئ سلسلة عادات تبدأ بـ $anchor",
                    confidenceScore = 0.89f
                )
            )
        }

        // Fallback generic guidance
        if (insights.isEmpty()) {
            insights.add(
                AiCoachingAdvice(
                    title = "Prime Your Environment",
                    titleAr = "جهّز بيئتك للإنجاز",
                    tip = "Reduce activation friction. Put your workout shoes out the night before or open your book on your desk.",
                    tipAr = "قلل مقاومة البداية. جهز أدوات العادة مسبقاً ليسهل عليك البدء فوراً.",
                    actionSuggestion = "Set up visual triggers",
                    actionSuggestionAr = "جهّز محفزات بصرية حولك",
                    confidenceScore = 0.80f
                )
            )
        }

        return insights
    }
}
