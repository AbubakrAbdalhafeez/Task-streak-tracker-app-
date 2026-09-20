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
            quoteEn = "We are what we repeatedly do. Excellence, then, is not an act, but a habit.",
            quoteAr = "نحن ما نفعله مراراً وتكراراً. فالتميز إذن ليس عملاً منفرداً بل عادة.",
            author = "Will Durant",
            category = "Consistency"
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
            category = "Systems"
        ),
        Quote(
            quoteEn = "Action is the foundational key to all success.",
            quoteAr = "العمل المستمر هو المفتاح الأساسي لكل نجاح.",
            author = "Pablo Picasso",
            category = "Action"
        ),
        Quote(
            quoteEn = "Do what you can, with what you have, where you are.",
            quoteAr = "افعل ما تستطيع، بما لديك، في المكان الذي أنت فيه.",
            author = "Theodore Roosevelt",
            category = "Focus"
        ),
        Quote(
            quoteEn = "Success isn't always about greatness. It's about consistency. Consistent hard work leads to success.",
            quoteAr = "النجاح لا يتعلق دوماً بالعظمة، بل بالاستمرارية؛ العمل الجاد المستمر يولد النجاح.",
            author = "Dwayne Johnson",
            category = "Consistency"
        ),
        Quote(
            quoteEn = "The secret of getting ahead is getting started.",
            quoteAr = "سر التقدم نحو الأمام هو البدء في الخطوة الأولى.",
            author = "Mark Twain",
            category = "Motivation"
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
