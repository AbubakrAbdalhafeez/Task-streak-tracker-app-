package com.abubakr.taskstreak.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private fun buildColorScheme(isDark: Boolean, palette: String): androidx.compose.material3.ColorScheme {
    val (primary, primaryVariant, secondary, tertiary) = when (palette) {
        "FOREST" -> Quad(ForestPrimary, ForestPrimaryVariant, ForestSecondary, ForestTertiary)
        "OCEAN" -> Quad(OceanPrimary, OceanPrimaryVariant, OceanSecondary, OceanTertiary)
        "TWILIGHT" -> Quad(TwilightPrimary, TwilightPrimaryVariant, TwilightSecondary, TwilightTertiary)
        else -> if (isDark) {
            Quad(LightModeBlueSecondary, LightModeBlue, LightModeBlueTertiary, Color(0xFF1E3A8A))
        } else {
            Quad(LightModeBlue, LightModeBlueVariant, LightModeBlueSecondary, LightModeBlueTertiary)
        }
    }

    return if (isDark) {
        darkColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primaryVariant,
            onPrimaryContainer = Color.White,
            secondary = secondary,
            onSecondary = Color.Black,
            secondaryContainer = tertiary,
            onSecondaryContainer = Color.White,
            tertiary = tertiary,
            onTertiary = Color.White,
            background = DarkBackground,
            onBackground = DarkTextPrimary,
            surface = DarkSurface,
            onSurface = DarkTextPrimary,
            surfaceVariant = DarkSurfaceVariant,
            onSurfaceVariant = DarkTextSecondary,
            error = DangerRed,
            onError = Color.White,
            outline = DarkBorder
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primary.copy(alpha = 0.2f),
            onPrimaryContainer = Color.Black,
            secondary = secondary,
            onSecondary = Color.Black,
            secondaryContainer = secondary.copy(alpha = 0.2f),
            onSecondaryContainer = Color.Black,
            tertiary = tertiary,
            onTertiary = Color.White,
            background = LightBackground,
            onBackground = LightTextPrimary,
            surface = LightSurface,
            onSurface = LightTextPrimary,
            surfaceVariant = LightSurfaceVariant,
            onSurfaceVariant = LightTextSecondary,
            error = DangerRed,
            onError = Color.White,
            outline = LightBorder
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun TaskStreakTheme(
    themePreference: String = "SYSTEM",
    colorPalette: String = "FLAME",
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themePreference) {
        "DARK" -> true
        "LIGHT" -> false
        else -> systemInDark
    }

    val colorScheme = buildColorScheme(isDark, colorPalette)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    TaskStreakTheme(
        themePreference = if (darkTheme) "DARK" else "LIGHT",
        content = content
    )
}

