package com.abubakr.taskstreak.util

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Handler
import android.os.Looper
import android.service.wallpaper.WallpaperService
import android.view.SurfaceHolder
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class StreakWallpaperService : WallpaperService() {
    override fun onCreateEngine(): Engine {
        return StreakEngine()
    }

    private inner class StreakEngine : Engine() {
        private val handler = Handler(Looper.getMainLooper())
        private var visible = false
        private val ringRect = RectF()

        private val bgPaint = Paint().apply {
            color = Color.parseColor("#121214")
            style = Paint.Style.FILL
        }

        private val flamePaint = Paint().apply {
            color = Color.parseColor("#FF6B35")
            style = Paint.Style.FILL
            isAntiAlias = true
        }

        private val ringBgPaint = Paint().apply {
            color = Color.parseColor("#26262B")
            style = Paint.Style.STROKE
            strokeWidth = 14f
            isAntiAlias = true
        }

        private val ringProgressPaint = Paint().apply {
            color = Color.parseColor("#FF6B35")
            style = Paint.Style.STROKE
            strokeWidth = 14f
            strokeCap = Paint.Cap.ROUND
            isAntiAlias = true
        }

        private val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 54f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
            isFakeBoldText = true
        }

        private val subTextPaint = Paint().apply {
            color = Color.parseColor("#9E9EA7")
            textSize = 28f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        private val drawRunnable = Runnable { drawFrame() }

        override fun onVisibilityChanged(visible: Boolean) {
            this.visible = visible
            if (visible) {
                handler.post(drawRunnable)
            } else {
                handler.removeCallbacks(drawRunnable)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            visible = false
            handler.removeCallbacks(drawRunnable)
        }

        private fun drawFrame() {
            val holder = surfaceHolder
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                if (canvas != null) {
                    val width = canvas.width.toFloat()
                    val height = canvas.height.toFloat()
                    val centerX = width / 2f
                    val centerY = height / 2f - 60f

                    // Draw dark background
                    canvas.drawRect(0f, 0f, width, height, bgPaint)

                    // Draw circular progress ring
                    val radius = 160f
                    ringRect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
                    canvas.drawArc(ringRect, 0f, 360f, false, ringBgPaint)
                    // Sample progress (75% of daily habits done)
                    canvas.drawArc(ringRect, -90f, 270f, false, ringProgressPaint)

                    // Center flame icon & streak indicator
                    textPaint.textSize = 64f
                    canvas.drawText("🔥", centerX, centerY - 10f, textPaint)

                    textPaint.textSize = 34f
                    canvas.drawText("STREAK ACTIVE", centerX, centerY + 50f, textPaint)

                    // Date
                    val dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
                    subTextPaint.textSize = 30f
                    canvas.drawText(dateStr, centerX, centerY + radius + 80f, subTextPaint)

                    subTextPaint.textSize = 24f
                    canvas.drawText("Task Streak Tracker Live Visualizer", centerX, centerY + radius + 120f, subTextPaint)
                }
            } finally {
                if (canvas != null) {
                    try {
                        holder.unlockCanvasAndPost(canvas)
                    } catch (_: Exception) {}
                }
            }

            handler.removeCallbacks(drawRunnable)
            if (visible) {
                // Redraw periodically (every 1 minute is extremely battery friendly)
                handler.postDelayed(drawRunnable, 60_000)
            }
        }
    }
}
