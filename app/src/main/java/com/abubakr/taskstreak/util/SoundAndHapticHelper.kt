package com.abubakr.taskstreak.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

object SoundAndHapticHelper {

    private var toneGenerator: ToneGenerator? = null

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 70)
        } catch (_: Exception) {}
    }

    fun playSuccessFeedback(context: Context, soundEnabled: Boolean = true, hapticsEnabled: Boolean = true) {
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
            } catch (_: Exception) {}
        }
        if (hapticsEnabled) {
            vibrate(context, 70)
        }
    }

    fun playStreakCelebrationFeedback(context: Context, soundEnabled: Boolean = true, hapticsEnabled: Boolean = true) {
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
            } catch (_: Exception) {}
        }
        if (hapticsEnabled) {
            vibratePattern(context, longArrayOf(0, 50, 60, 100))
        }
    }

    fun playTickFeedback(context: Context, soundEnabled: Boolean = true, hapticsEnabled: Boolean = true) {
        if (soundEnabled) {
            try {
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 50)
            } catch (_: Exception) {}
        }
        if (hapticsEnabled) {
            vibrate(context, 25)
        }
    }

    private fun vibrate(context: Context, durationMs: Long) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(durationMs)
            }
        } catch (_: Exception) {}
    }

    private fun vibratePattern(context: Context, pattern: LongArray) {
        try {
            val vibrator = getVibrator(context) ?: return
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        } catch (_: Exception) {}
    }

    private fun getVibrator(context: Context): Vibrator? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }
}
