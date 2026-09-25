package com.abubakr.taskstreak.util

import java.time.LocalDate

data class Quote(
    val quoteEn: String,
    val quoteAr: String,
    val author: String,
    val category: String
)

object MotivationEngine {
    private val quotes = listOf(
        Quote(
            quoteEn = "Small progress is still progress.",
            quoteAr = "التقدم البسيط يظل تقدماً يستحق الفخر.",
            author = "Anonymous",
            category = "Progress"
        ),
        Quote(
            quoteEn = "We are what we repeatedly do. Excellence, then, is not an act, but a habit.",
            quoteAr = "نحن ما نفعله مراراً وتكراراً. فالتميز إذن ليس عملاً منفرداً بل عادة.",
            author = "Will Durant",
            category = "Habits"
        ),
        Quote(
            quoteEn = "Small disciplines repeated with consistency every day lead to great achievements.",
            quoteAr = "الانضباطات الصغيرة المكررة بثبات يومياً تقود إلى إنجازات عظيمة.",
            author = "John C. Maxwell",
            category = "Discipline"
        ),
        Quote(
            quoteEn = "You do not rise to the level of your goals. You fall to the level of your systems.",
            quoteAr = "أنت لا ترتقي إلى مستوى أهدافك، بل تهبط إلى مستوى أنظمتك.",
            author = "James Clear",
            category = "Habits"
        ),
        Quote(
            quoteEn = "Success is the sum of small efforts, repeated day in and day out.",
            quoteAr = "النجاح هو مجموع الجهود الصغيرة، المتكررة يوماً بعد يوم.",
            author = "Robert Collier",
            category = "Consistency"
        ),
        Quote(
            quoteEn = "Consistency is what transforms average into excellence.",
            quoteAr = "الاستمرارية هي ما يحول العادي إلى مستوى استثنائي.",
            author = "Tony Robbins",
            category = "Consistency"
        ),
        Quote(
            quoteEn = "Action is the foundational key to all success.",
            quoteAr = "العمل والمبادرة هما المفتاح الأساسي لكل نجاح.",
            author = "Pablo Picasso",
            category = "Productivity"
        ),
        Quote(
            quoteEn = "Discipline is choosing between what you want now and what you want most.",
            quoteAr = "الانضباط هو الاختيار بين ما تريده الآن وما تريده أكثر في مستقبلك.",
            author = "Abraham Lincoln",
            category = "Discipline"
        ),
        Quote(
            quoteEn = "The secret of getting ahead is getting started.",
            quoteAr = "سر التقدم نحو الأمام هو البدء في الخطوة الأولى دون تردد.",
            author = "Mark Twain",
            category = "Productivity"
        ),
        Quote(
            quoteEn = "Great things are done by a series of small things brought together.",
            quoteAr = "تتحقق الإنجازات العظيمة عبر سلسلة متتابعة من الخطوات الصغيرة.",
            author = "Vincent Van Gogh",
            category = "Achievement"
        ),
        Quote(
            quoteEn = "Be not afraid of going slowly, be afraid only of standing still.",
            quoteAr = "لا تخف من السير ببطء، بل خف فقط من البقاء واقفاً في مكانك.",
            author = "Chinese Proverb",
            category = "Personal Growth"
        ),
        Quote(
            quoteEn = "Habits are the compound interest of self-improvement.",
            quoteAr = "العادات هي الفائدة المركبة لتطوير الذات.",
            author = "James Clear",
            category = "Habits"
        ),
        Quote(
            quoteEn = "Focus on progress, not perfection.",
            quoteAr = "ركز على التقدم المستمر، وليس على المثالية المطلقة.",
            author = "Bill Phillips",
            category = "Progress"
        ),
        Quote(
            quoteEn = "What you do every day matters more than what you do once in a while.",
            quoteAr = "ما تفعله كل يوم أكثر أهمية وتأثيراً مما تفعله من حين لآخر.",
            author = "Gretchen Rubin",
            category = "Personal Growth"
        )
    )

    fun getTodayQuote(): Quote {
        val day = LocalDate.now().dayOfYear
        return quotes[day % quotes.size]
    }

    fun getCoachingInsight(completedToday: Int, totalScheduled: Int, bestStreak: Int): String {
        return when {
            totalScheduled > 0 && completedToday == totalScheduled ->
                "🎉 Perfect Day! You completed all scheduled habits. You're building iron discipline!"
            completedToday > 0 ->
                "🔥 Momentum is active! You completed $completedToday/$totalScheduled tasks today. Finish the rest to protect your streaks!"
            bestStreak >= 7 ->
                "⚡ You have active streaks of $bestStreak days! Keep the flame burning bright today."
            else ->
                "🌱 Every master was once a beginner. Complete just one habit to kickstart your streak!"
        }
    }
}
