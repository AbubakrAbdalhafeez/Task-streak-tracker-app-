package com.abubakr.taskstreak.util

import com.abubakr.taskstreak.data.model.RecurrenceType
import com.abubakr.taskstreak.data.model.TaskEntity
import java.util.Locale
import java.util.regex.Pattern

data class ParsedVoiceTask(
    val title: String,
    val recurrenceType: RecurrenceType = RecurrenceType.DAILY,
    val reminderTime: String? = null,
    val category: String = "General"
)

object VoiceInputHelper {
    /**
     * Parses natural language spoken strings in Arabic or English into structured task fields.
     */
    fun parseSpokenText(rawInput: String): ParsedVoiceTask {
        var text = rawInput.trim()

        // Strip common prefixes
        val prefixes = listOf("add task", "add habit", "create task", "أضف مهمة", "أضف عادة", "سجل مهمة", "جديدة", "new task")
        for (prefix in prefixes) {
            if (text.startsWith(prefix, ignoreCase = true)) {
                text = text.substring(prefix.length).trim()
                if (text.startsWith(":") || text.startsWith("-")) {
                    text = text.substring(1).trim()
                }
                break
            }
        }

        // Detect Time (e.g. "at 9am", "at 20:00", "الساعة 9")
        var detectedTime: String? = null
        val timePatternEn = Pattern.compile("(?:at\\s+)?(\\d{1,2})(?::(\\d{2}))?\\s*(am|pm)?", Pattern.CASE_INSENSITIVE)
        val matcherEn = timePatternEn.matcher(text)
        if (matcherEn.find()) {
            val hourStr = matcherEn.group(1)
            val minStr = matcherEn.group(2) ?: "00"
            val ampm = matcherEn.group(3)?.lowercase(Locale.ROOT)

            var hour = hourStr?.toIntOrNull() ?: 9
            if (ampm == "pm" && hour < 12) hour += 12
            if (ampm == "am" && hour == 12) hour = 0

            detectedTime = String.format(Locale.getDefault(), "%02d:%02d", hour.coerceIn(0, 23), minStr.toIntOrNull()?.coerceIn(0, 59) ?: 0)
            text = text.replace(matcherEn.group(0) ?: "", "").trim()
        }

        // Arabic time detection (الساعة X)
        if (detectedTime == null && text.contains("الساعة")) {
            val arPattern = Pattern.compile("الساعة\\s+(\\d{1,2})", Pattern.CASE_INSENSITIVE)
            val arMatcher = arPattern.matcher(text)
            if (arMatcher.find()) {
                val hour = arMatcher.group(1)?.toIntOrNull() ?: 9
                detectedTime = String.format(Locale.getDefault(), "%02d:00", hour.coerceIn(0, 23))
                text = text.replace(arMatcher.group(0) ?: "", "").trim()
            }
        }

        // Detect Category
        var detectedCategory = "General"
        val lower = text.lowercase(Locale.ROOT)
        when {
            lower.contains("sport") || lower.contains("workout") || lower.contains("gym") || text.contains("رياضة") || text.contains("تمرين") -> {
                detectedCategory = "Fitness"
            }
            lower.contains("study") || lower.contains("read") || text.contains("دراسة") || text.contains("مذاكرة") || text.contains("قراءة") -> {
                detectedCategory = "Study"
            }
            lower.contains("english") || text.contains("إنجليزي") || text.contains("انجليزي") -> {
                detectedCategory = "English"
            }
            lower.contains("work") || text.contains("عمل") || text.contains("شغل") -> {
                detectedCategory = "Work"
            }
        }

        // Clean trailing punctuation or connector words
        text = text.replace(Regex("^(daily|once|يوميا|مرة واحدة)\\s*"), "")
            .replace(Regex("\\s+(daily|يوميا)$"), "")
            .trim()

        if (text.isBlank()) {
            text = "Voice Task"
        }

        return ParsedVoiceTask(
            title = text.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() },
            recurrenceType = RecurrenceType.DAILY,
            reminderTime = detectedTime,
            category = detectedCategory
        )
    }
}
