package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Metro Theme Palette
val MetroPrimary = Color(0xFF0D47A1)
val MetroPrimaryLight = Color(0xFF5472D3)
val MetroPrimaryDark = Color(0xFF002171)

val MetroAccent = Color(0xFFFF6F00)
val MetroAccentLight = Color(0xFFFFA040)
val MetroAccentDark = Color(0xFFC43E00)

val MetroSuccess = Color(0xFF2E7D32)
val MetroWarning = Color(0xFFF57F17)
val MetroError = Color(0xFFC62828)

// Dark Theme Surface & Backgrounds
val MetroDarkBg = Color(0xFF10141D)
val MetroDarkSurface = Color(0xFF1B212D)
val MetroDarkSurfaceVariant = Color(0xFF252D3D)
val MetroDarkOnSurface = Color(0xFFE2E8F0)

// Light Theme Surface & Backgrounds
val MetroLightBg = Color(0xFFF7F9FC)
val MetroLightSurface = Color(0xFFFFFFFF)
val MetroLightSurfaceVariant = Color(0xFFEEF2F6)
val MetroLightOnSurface = Color(0xFF1A202C)

// Standard Indian Metro Line Color Palette
object MetroLineColors {
    val Red = Color(0xFFE53935)
    val Yellow = Color(0xFFFDD835)
    val Blue = Color(0xFF1E88E5)
    val Violet = Color(0xFF8E24AA)
    val Pink = Color(0xFFEC407A)
    val Magenta = Color(0xFFD81B60)
    val Green = Color(0xFF43A047)
    val Orange = Color(0xFFFB8C00)
    val Grey = Color(0xFF757575)
    val Aqua = Color(0xFF00BCD4)
    val Purple = Color(0xFF7E57C2)
    val Silver = Color(0xFF9E9E9E)
    val Gold = Color(0xFFFFB300)

    fun getColorForLine(codeOrName: String): Color {
        val lower = codeOrName.lowercase()
        return when {
            lower.contains("red") -> Red
            lower.contains("yellow") -> Yellow
            lower.contains("blue") -> Blue
            lower.contains("violet") -> Violet
            lower.contains("pink") -> Pink
            lower.contains("magenta") -> Magenta
            lower.contains("green") -> Green
            lower.contains("airport") || lower.contains("orange") -> Orange
            lower.contains("grey") || lower.contains("gray") -> Grey
            lower.contains("aqua") -> Aqua
            lower.contains("purple") -> Purple
            lower.contains("gold") -> Gold
            else -> MetroPrimary
        }
    }
}
