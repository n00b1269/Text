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
    primary = XcodeBlue80,
    onPrimary = Color(0xFF003554),
    primaryContainer = Color(0xFF004D74),
    onPrimaryContainer = Color(0xFFC2E8FF),
    secondary = XcodeIndigo80,
    onSecondary = Color(0xFF1E1E5C),
    secondaryContainer = Color(0xFF333380),
    onSecondaryContainer = Color(0xFFDFE0FF),
    tertiary = SwiftOrange80,
    background = DarkSlateBackground,
    surface = DarkSlateSurface,
    onBackground = Color(0xFFE2E8F0),
    onSurface = Color(0xFFE2E8F0)
)

private val LightColorScheme = lightColorScheme(
    primary = XcodeBlue40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC2E8FF),
    onPrimaryContainer = Color(0xFF001E30),
    secondary = XcodeIndigo40,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDFE0FF),
    onSecondaryContainer = Color(0xFF090069),
    tertiary = SwiftOrange40,
    background = LightBackground,
    surface = LightSurface,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
