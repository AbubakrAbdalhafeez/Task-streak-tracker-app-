package com.abubakr.taskstreak.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.RectF
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.Log
import androidx.core.content.FileProvider
import com.abubakr.taskstreak.data.model.TaskEntity
import java.io.File
import java.io.FileOutputStream

data class FriendStreakBadge(
    val friendName: String,
    val taskTitle: String,
    val streakDays: Int,
    val dateVerified: String
)

object SocialShareHelper {
    private const val TAG = "SocialShareHelper"

    /**
     * Generates a beautiful branded card image for the achievement
     */
    fun createAchievementBitmap(
        title: String,
        description: String,
        emoji: String = "🏆",
        streakDays: Int? = null
    ): Bitmap {
        val width = 1080
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background Gradient (Modern Deep Midnight to Indigo/Blue)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(0xFF0F172A.toInt(), 0xFF1E293B.toInt(), 0xFF0F172A.toInt()),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Glowing outer card container
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                100f, 100f, (width - 100).toFloat(), (height - 100).toFloat(),
                intArrayOf(0xFF1E3A8A.toInt(), 0xFF172554.toInt()),
                null, Shader.TileMode.CLAMP
            )
        }
        val cardRect = RectF(80f, 80f, (width - 80).toFloat(), (height - 80).toFloat())
        canvas.drawRoundRect(cardRect, 48f, 48f, cardPaint)

        // Card Border with subtle glow
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 6f
            color = 0xFF3B82F6.toInt()
        }
        canvas.drawRoundRect(cardRect, 48f, 48f, borderPaint)

        // App Logo / Header Pill
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF2563EB.toInt()
        }
        val pillRect = RectF(290f, 140f, 790f, 210f)
        canvas.drawRoundRect(pillRect, 35f, 35f, pillPaint)

        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 34f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText("TASK STREAK TRACKER", width / 2f, 188f, headerTextPaint)

        // Large Badge / Emoji Circle
        val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                width / 2f - 130f, 280f, width / 2f + 130f, 540f,
                intArrayOf(0xFFF59E0B.toInt(), 0xFFEF4444.toInt()),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(width / 2f, 410f, 130f, circlePaint)

        val emojiPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textSize = 120f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(emoji, width / 2f, 450f, emojiPaint)

        // "ACHIEVEMENT UNLOCKED" banner text
        val subHeaderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF93C5FD.toInt()
            textSize = 32f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            letterSpacing = 0.08f
        }
        val subLabel = if (streakDays != null && streakDays > 0) {
            "🔥 $streakDays-DAY ACTIVE STREAK! 🔥"
        } else {
            "🏆 MILESTONE REACHED 🏆"
        }
        canvas.drawText(subLabel, width / 2f, 610f, subHeaderPaint)

        // Achievement Title (Bold & Crisp)
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFFFFFF.toInt()
            textSize = 54f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        // Truncate title if extremely long
        val safeTitle = if (title.length > 28) title.take(26) + "..." else title
        canvas.drawText(safeTitle, width / 2f, 690f, titlePaint)

        // Achievement Description (Soft contrast)
        val descPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFCBD5E1.toInt()
            textSize = 36f
            textAlign = Paint.Align.CENTER
        }
        val safeDesc = if (description.length > 55) description.take(52) + "..." else description
        canvas.drawText(safeDesc, width / 2f, 765f, descPaint)

        // Bottom Motivation Box
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF38BDF8.toInt()
            textSize = 32f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText("Building daily discipline, one day at a time.", width / 2f, 880f, footerPaint)

        val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF64748B.toInt()
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Available Offline & Cloud Sync on Android", width / 2f, 930f, watermarkPaint)

        return bitmap
    }

    /**
     * Saves bitmap to cache and returns FileProvider Uri safely
     */
    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cacheFolder = File(context.cacheDir, "shared_images").apply {
                if (!exists()) mkdirs()
            }
            // Clear any stale share images older than 1 hour to save disk space
            try {
                cacheFolder.listFiles()?.forEach { file ->
                    if (System.currentTimeMillis() - file.lastModified() > 3600_000L) {
                        file.delete()
                    }
                }
            } catch (_: Exception) {}

            val imageFile = File(cacheFolder, "streak_achievement_${System.currentTimeMillis()}.png")
            FileOutputStream(imageFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 95, out)
                out.flush()
            }
            val authority = "${context.packageName}.fileprovider"
            FileProvider.getUriForFile(context, authority, imageFile)
        } catch (e: Throwable) {
            Log.e(TAG, "Failed to save bitmap to cache", e)
            null
        }
    }

    /**
     * Generates a shareable streak text & digital passport with visual card
     */
    fun shareStreakAchievement(context: Context, task: TaskEntity, streakDays: Int) {
        try {
            val shareText = """
                🔥 Task Streak Milestone!
                I've maintained a $streakDays-day active streak for "${task.title}"!
                
                Consistency is key! Track your streaks with Task Streak Tracker 🔥
            """.trimIndent()

            var imageUri: Uri? = null
            try {
                val bitmap = createAchievementBitmap(
                    title = task.title,
                    description = "$streakDays-Day Consecutive Streak Maintained!",
                    emoji = "🔥",
                    streakDays = streakDays
                )
                imageUri = saveBitmapToCache(context, bitmap)
            } catch (e: Throwable) {
                Log.w(TAG, "Could not generate streak bitmap, continuing with text share", e)
            }

            dispatchShareIntent(context, shareText, imageUri, "Share Streak Milestone")
        } catch (e: Throwable) {
            Log.e(TAG, "Error sharing streak achievement", e)
            fallbackTextShare(context, "🔥 Maintained a $streakDays-day streak for \"${task.title}\" on TaskStreak!")
        }
    }

    fun shareHabitStreak(context: Context, task: TaskEntity, streakDays: Int) {
        shareStreakAchievement(context, task, streakDays)
    }

    fun shareAchievement(
        context: Context,
        title: String,
        description: String,
        emoji: String = "🏆"
    ) {
        try {
            val shareText = """
                🏆 Achievement Unlocked: $title!
                $description
                
                Building healthy daily habits with Task Streak Tracker! 🔥
            """.trimIndent()

            var imageUri: Uri? = null
            try {
                val bitmap = createAchievementBitmap(
                    title = title,
                    description = description,
                    emoji = emoji
                )
                imageUri = saveBitmapToCache(context, bitmap)
            } catch (e: Throwable) {
                Log.w(TAG, "Could not generate achievement bitmap, continuing with text share", e)
            }

            dispatchShareIntent(context, shareText, imageUri, "Share Achievement")
        } catch (e: Throwable) {
            Log.e(TAG, "Error sharing achievement", e)
            fallbackTextShare(context, "🏆 Achievement Unlocked: $title! $description on TaskStreak!")
        }
    }

    /**
     * Generates an aesthetic branded quote card bitmap for social sharing (Card Image + Text)
     */
    fun createQuoteCardBitmap(
        quoteEn: String,
        quoteAr: String?,
        author: String,
        isArabic: Boolean = false
    ): Bitmap {
        val width = 1080
        val height = 1080
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background Gradient (Deep midnight slate to indigo)
        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                intArrayOf(0xFF090D16.toInt(), 0xFF111827.toInt(), 0xFF0B1222.toInt()),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        // Subtle decorative ambient background glows
        val glowPaint1 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                200f, 200f, 350f,
                0x333B82F6.toInt(), 0x00000000.toInt(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle(200f, 200f, 350f, glowPaint1)

        val glowPaint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = RadialGradient(
                (width - 200).toFloat(), (height - 250).toFloat(), 400f,
                0x288B5CF6.toInt(), 0x00000000.toInt(),
                Shader.TileMode.CLAMP
            )
        }
        canvas.drawCircle((width - 200).toFloat(), (height - 250).toFloat(), 400f, glowPaint2)

        // 2. Main Card Container with glassmorphic look
        val cardRect = RectF(70f, 70f, (width - 70).toFloat(), (height - 70).toFloat())
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(
                100f, 100f, (width - 100).toFloat(), (height - 100).toFloat(),
                intArrayOf(0xDD131D33.toInt(), 0xEE0F172A.toInt()),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(cardRect, 44f, 44f, cardPaint)

        // Card Border Gradient
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 4.5f
            shader = LinearGradient(
                70f, 70f, (width - 70).toFloat(), (height - 70).toFloat(),
                intArrayOf(0xFF38BDF8.toInt(), 0xFF818CF8.toInt(), 0xFF3B82F6.toInt()),
                null, Shader.TileMode.CLAMP
            )
        }
        canvas.drawRoundRect(cardRect, 44f, 44f, borderPaint)

        // 3. Header Pill: "✨ DAILY INSPIRATION" / "✨ حكمة اليوم"
        val pillRect = RectF(300f, 120f, 780f, 185f)
        val pillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF1E293B.toInt()
        }
        canvas.drawRoundRect(pillRect, 32.5f, 32.5f, pillPaint)

        val pillBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = 0xFF38BDF8.toInt()
        }
        canvas.drawRoundRect(pillRect, 32.5f, 32.5f, pillBorderPaint)

        val headerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF38BDF8.toInt()
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
            letterSpacing = 0.06f
        }
        val headerTitle = if (isArabic) "✨ حكمة اليوم • TASK STREAK" else "✨ DAILY INSPIRATION • TASK STREAK"
        canvas.drawText(headerTitle, width / 2f, 162f, headerTextPaint)

        // 4. Large Quotation Mark
        val quoteMarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0x6638BDF8.toInt()
            textSize = 90f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText("❝", width / 2f, 280f, quoteMarkPaint)

        // 5. Quote Text rendering with multiline wrapping
        val primaryText = if (isArabic && !quoteAr.isNullOrBlank()) quoteAr else quoteEn
        val secondaryText = if (isArabic && !quoteAr.isNullOrBlank()) quoteEn else quoteAr

        val mainQuotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF8FAFC.toInt()
            textSize = if (primaryText.length > 120) 38f else if (primaryText.length > 70) 44f else 50f
            isFakeBoldText = true
        }

        val maxWidth = 860
        val mainLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(primaryText, 0, primaryText.length, mainQuotePaint, maxWidth)
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setLineSpacing(10f, 1.25f)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(primaryText, mainQuotePaint, maxWidth, Layout.Alignment.ALIGN_CENTER, 1.25f, 10f, false)
        }

        // Secondary quote layout (subtext translation)
        var secondaryLayout: StaticLayout? = null
        if (!secondaryText.isNullOrBlank()) {
            val subQuotePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xFF94A3B8.toInt()
                textSize = if (secondaryText.length > 100) 28f else 32f
            }
            secondaryLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                StaticLayout.Builder.obtain(secondaryText, 0, secondaryText.length, subQuotePaint, maxWidth)
                    .setAlignment(Layout.Alignment.ALIGN_CENTER)
                    .setLineSpacing(8f, 1.2f)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                StaticLayout(secondaryText, subQuotePaint, maxWidth, Layout.Alignment.ALIGN_CENTER, 1.2f, 8f, false)
            }
        }

        val totalTextHeight = mainLayout.height + (secondaryLayout?.height?.plus(30) ?: 0)
        // Center text vertically in the region between 310f and 770f
        val availableMiddleY = 540f
        val startY = (availableMiddleY - (totalTextHeight / 2f)).coerceAtLeast(310f)

        canvas.save()
        canvas.translate((width - maxWidth) / 2f, startY)
        mainLayout.draw(canvas)
        canvas.restore()

        if (secondaryLayout != null) {
            val secY = startY + mainLayout.height + 25f
            canvas.save()
            canvas.translate((width - maxWidth) / 2f, secY)
            secondaryLayout.draw(canvas)
            canvas.restore()
        }

        // 6. Accent Divider Line & Author
        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFF59E0B.toInt()
            strokeWidth = 3f
        }
        val dividerY = (startY + totalTextHeight + 45f).coerceAtMost(810f)
        canvas.drawLine(width / 2f - 60f, dividerY, width / 2f + 60f, dividerY, dividerPaint)

        val authorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFFFBBF24.toInt() // Warm Golden Amber
            textSize = 34f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        canvas.drawText("— $author", width / 2f, dividerY + 50f, authorPaint)

        // 7. Footer Brand Bar
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF64748B.toInt()
            textSize = 24f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("TaskStreak 🔥 • Build Consistency Every Day", width / 2f, 930f, footerPaint)

        val watermarkPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = 0xFF475569.toInt()
            textSize = 20f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Offline Habit & Streak Tracker on Android", width / 2f, 965f, watermarkPaint)

        return bitmap
    }

    /**
     * Shares a daily quote as both a visual card image (Bitmap) and accompanying text
     */
    fun shareQuote(
        context: Context,
        quoteEn: String,
        quoteAr: String?,
        author: String,
        isArabic: Boolean = false
    ) {
        try {
            val shareText = buildString {
                if (isArabic && !quoteAr.isNullOrBlank()) {
                    append("“$quoteAr”\n")
                    append("— $author\n\n")
                    append("“$quoteEn”\n\n")
                } else {
                    append("“$quoteEn”\n")
                    append("— $author\n\n")
                    if (!quoteAr.isNullOrBlank()) {
                        append("“$quoteAr”\n\n")
                    }
                }
                append("Track your habits & streaks with TaskStreak 🔥")
            }

            var imageUri: Uri? = null
            try {
                val bitmap = createQuoteCardBitmap(
                    quoteEn = quoteEn,
                    quoteAr = quoteAr,
                    author = author,
                    isArabic = isArabic
                )
                imageUri = saveBitmapToCache(context, bitmap)
            } catch (e: Throwable) {
                Log.w(TAG, "Could not generate quote card bitmap, falling back to text share", e)
            }

            dispatchShareIntent(context, shareText, imageUri, if (isArabic) "مشاركة بطاقة الاقتباس" else "Share Quote Card")
        } catch (e: Throwable) {
            Log.e(TAG, "Error sharing quote", e)
            fallbackTextShare(context, "“$quoteEn” — $author\n#TaskStreak")
        }
    }

    private fun dispatchShareIntent(
        context: Context,
        text: String,
        imageUri: Uri?,
        chooserTitle: String
    ) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                if (imageUri != null) {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, imageUri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    clipData = android.content.ClipData.newRawUri("Quote Image", imageUri)
                } else {
                    type = "text/plain"
                }
                putExtra(Intent.EXTRA_TEXT, text)
                putExtra(Intent.EXTRA_SUBJECT, chooserTitle)
            }

            // Explicitly grant URI permission to all potential matching receiver apps
            if (imageUri != null) {
                try {
                    val resInfoList = context.packageManager.queryIntentActivities(
                        sendIntent,
                        android.content.pm.PackageManager.MATCH_DEFAULT_ONLY
                    )
                    for (resolveInfo in resInfoList) {
                        val packageName = resolveInfo.activityInfo.packageName
                        try {
                            context.grantUriPermission(
                                packageName,
                                imageUri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                            )
                        } catch (_: Exception) {}
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Could not pre-grant URI permissions", e)
                }
            }

            val chooser = Intent.createChooser(sendIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(chooser)
        } catch (e: Throwable) {
            Log.e(TAG, "dispatchShareIntent failed, falling back to basic text share", e)
            fallbackTextShare(context, text)
        }
    }

    private fun fallbackTextShare(context: Context, text: String) {
        try {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, text)
                type = "text/plain"
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(shareIntent)
        } catch (e: Exception) {
            Log.e(TAG, "Fallback share also failed", e)
        }
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

