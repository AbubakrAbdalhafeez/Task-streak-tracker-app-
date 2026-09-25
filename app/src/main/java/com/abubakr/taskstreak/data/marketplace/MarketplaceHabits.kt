package com.abubakr.taskstreak.data.marketplace

data class MarketplaceHabit(
    val id: String,
    val title: String,
    val titleAr: String,
    val author: String,
    val authorAr: String,
    val rating: Float,
    val reviewCount: Int,
    val downloads: Int,
    val category: String,
    val categoryAr: String,
    val frequency: String,
    val suggestedReminder: String,
    val description: String,
    val descriptionAr: String,
    val benefits: List<String>,
    val benefitsAr: List<String>
) {
    fun localizedTitle(isArabic: Boolean): String = if (isArabic) titleAr else title
    fun localizedDescription(isArabic: Boolean): String = if (isArabic) descriptionAr else description
    fun localizedCategory(isArabic: Boolean): String = if (isArabic) categoryAr else category
    fun localizedAuthor(isArabic: Boolean): String = if (isArabic) authorAr else author
    fun localizedBenefits(isArabic: Boolean): List<String> = if (isArabic) benefitsAr else benefits
}

object HabitMarketplaceRepository {
    private val marketplaceHabits = listOf(
        MarketplaceHabit(
            id = "mkt_1",
            title = "Morning Sunlight & Hydration",
            titleAr = "شمس الصباح وشرب الماء",
            author = "Dr. Andrew H.",
            authorAr = "د. أندرو هـ.",
            rating = 4.9f,
            reviewCount = 1420,
            downloads = 18500,
            category = "Health",
            categoryAr = "الصحة",
            frequency = "Daily",
            suggestedReminder = "07:00",
            description = "10 minutes of outdoor sunlight exposure paired with 500ml water right after waking up to reset circadian rhythm.",
            descriptionAr = "التعرض لأشعة الشمس الطبيعية لمدة 10 دقائق وشرب نصف لتر ماء فور الاستيقاظ لضبط الساعة البيولوجية.",
            benefits = listOf("Boosts dopamine", "Improves nighttime sleep", "Instant morning alertness"),
            benefitsAr = listOf("تعزيز الدوبامين والطاقة", "تحسين جودة النوم ليلاً", "يقظة ونشاط صباحي فوري")
        ),
        MarketplaceHabit(
            id = "mkt_2",
            title = "2-Minute Rule Habit Stacking",
            titleAr = "قاعدة الدقيقتين لبدء العادات",
            author = "James C. (Atomic Habits)",
            authorAr = "جيمس ك. (العادات الذرية)",
            rating = 4.8f,
            reviewCount = 980,
            downloads = 12400,
            category = "Productivity",
            categoryAr = "الإنتاجية",
            frequency = "Daily",
            suggestedReminder = "09:00",
            description = "Start any habit by taking action for just 120 seconds. Never break the chain.",
            descriptionAr = "ابدأ أي عادة جديدة بممارستها لمدة 120 ثانية فقط للتغلب على التسويف.",
            benefits = listOf("Eliminates procrastination", "Zero mental resistance", "Builds identity"),
            benefitsAr = listOf("القضاء على التسويف والمماطلة", "كسر حاجز المقاومة الذهنية", "بناء هوية الإنجاز المستمر")
        ),
        MarketplaceHabit(
            id = "mkt_3",
            title = "Box Breathing Reset",
            titleAr = "تنفس الصندوق (4-4-4-4)",
            author = "Navy SEAL Protocol",
            authorAr = "بروتوكول القوات الخاصة",
            rating = 4.9f,
            reviewCount = 740,
            downloads = 8900,
            category = "Mindfulness",
            categoryAr = "اليقظة الذهنية",
            frequency = "Daily",
            suggestedReminder = "14:00",
            description = "Inhale 4s, hold 4s, exhale 4s, hold 4s. 5 cycles to instantly downregulate the sympathetic nervous system.",
            descriptionAr = "استنشاق 4 ثوان، حبس 4 ثوان، زفير 4 ثوان، حبس 4 ثوان. خمس دورات لتهدئة التوتر تماماً.",
            benefits = listOf("Lowers cortisol", "Improves heart rate variability", "Sharpened mental clarity"),
            benefitsAr = listOf("تخفيض هرمون التوتر (الكورتيزول)", "تنظيم ضربات القلب", "صفاء ذهني وتركيز عالٍ")
        ),
        MarketplaceHabit(
            id = "mkt_4",
            title = "Evening Digital Sunset",
            titleAr = "غروب الشاشات المسائي",
            author = "Sleep Lab",
            authorAr = "مختبر النوم العميق",
            rating = 4.7f,
            reviewCount = 610,
            downloads = 6300,
            category = "Lifestyle",
            categoryAr = "نمط الحياة",
            frequency = "Daily",
            suggestedReminder = "21:30",
            description = "Shut off blue-light screens 45 minutes prior to sleep. Replace with reading or journaling.",
            descriptionAr = "إغلاق جميع الشاشات قبل النوم بـ 45 دقيقة واستبدالها بالقراءة أو التدوين.",
            benefits = listOf("Accelerates melatonin release", "Deeper REM cycles", "Wake up refreshed"),
            benefitsAr = listOf("تسريع إفراز الميلاتونين الطبيعي", "نوم عميق ومريح", "الاستيقاظ بكامل الحيوية")
        )
    )

    fun getMarketplaceHabits(): List<MarketplaceHabit> = marketplaceHabits
}
