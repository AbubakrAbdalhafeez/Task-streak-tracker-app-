package com.abubakr.taskstreak.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppLanguage = staticCompositionLocalOf { "en" }
val LocalIsArabic = staticCompositionLocalOf { false }

@Composable
fun isArabic(): Boolean = LocalIsArabic.current

fun localizedText(isArabic: Boolean, en: String, ar: String): String {
    return if (isArabic) ar else en
}
