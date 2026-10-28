package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MetroPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = MetroPrimaryDark,
    onPrimaryContainer = Color(0xFFD6E2FF),
    secondary = MetroAccentLight,
    onSecondary = Color.Black,
    secondaryContainer = MetroAccentDark,
    onSecondaryContainer = Color(0xFFFFDCC2),
    tertiary = Color(0xFF80DEEA),
    background = MetroDarkBg,
    onBackground = MetroDarkOnSurface,
    surface = MetroDarkSurface,
    onSurface = MetroDarkOnSurface,
    surfaceVariant = MetroDarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFB0B9C6),
    error = Color(0xFFEF9A9A),
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = MetroPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6E2FF),
    onPrimaryContainer = MetroPrimaryDark,
    secondary = MetroAccent,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDCC2),
    onSecondaryContainer = MetroAccentDark,
    tertiary = Color(0xFF00838F),
    background = MetroLightBg,
    onBackground = MetroLightOnSurface,
    surface = MetroLightSurface,
    onSurface = MetroLightOnSurface,
    surfaceVariant = MetroLightSurfaceVariant,
    onSurfaceVariant = Color(0xFF4A5568),
    error = MetroError,
    onError = Color.White
)

@Composable
fun MetroSathiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
