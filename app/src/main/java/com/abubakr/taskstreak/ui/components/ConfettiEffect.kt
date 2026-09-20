package com.abubakr.taskstreak.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import kotlin.random.Random

private data class ConfettiParticle(
    val xRatio: Float,
    val initialSpeedY: Float,
    val speedX: Float,
    val rotationSpeed: Float,
    val size: Float,
    val color: Color,
    val isCircle: Boolean
)

@Composable
fun ConfettiEffect(
    isVisible: Boolean,
    onAnimationEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isVisible) return

    val animProgress = remember { Animatable(0f) }

    val colors = listOf(
        Color(0xFFFF6B35), // Flame
        Color(0xFFFFD166), // Gold
        Color(0xFF06D6A0), // Emerald
        Color(0xFF118AB2), // Blue
        Color(0xFFEF476F), // Pink
        Color(0xFF8B5CF6)  // Purple
    )

    val particles = remember {
        List(65) {
            ConfettiParticle(
                xRatio = Random.nextFloat(),
                initialSpeedY = Random.nextFloat() * 1000f + 600f,
                speedX = (Random.nextFloat() - 0.5f) * 350f,
                rotationSpeed = (Random.nextFloat() - 0.5f) * 720f,
                size = Random.nextFloat() * 14f + 8f,
                color = colors[it % colors.size],
                isCircle = Random.nextBoolean()
            )
        }
    }

    LaunchedEffect(isVisible) {
        animProgress.snapTo(0f)
        animProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 2400, easing = LinearEasing)
        )
        onAnimationEnd()
    }

    val progress = animProgress.value

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height

        for (p in particles) {
            val currentX = (p.xRatio * w) + (p.speedX * progress)
            val currentY = -20f + (p.initialSpeedY * progress) + (0.5f * 980f * progress * progress * 0.3f)
            val currentAlpha = (1f - (progress * 0.85f)).coerceIn(0f, 1f)
            val rotation = p.rotationSpeed * progress

            if (currentY in -50f..(h + 50f) && currentX in -50f..(w + 50f)) {
                rotate(degrees = rotation, pivot = Offset(currentX, currentY)) {
                    val particleColor = p.color.copy(alpha = currentAlpha)
                    if (p.isCircle) {
                        drawCircle(
                            color = particleColor,
                            radius = p.size / 2f,
                            center = Offset(currentX, currentY)
                        )
                    } else {
                        drawRect(
                            color = particleColor,
                            topLeft = Offset(currentX - p.size / 2f, currentY - p.size / 2f),
                            size = Size(p.size, p.size * 0.6f)
                        )
                    }
                }
            }
        }
    }
}
