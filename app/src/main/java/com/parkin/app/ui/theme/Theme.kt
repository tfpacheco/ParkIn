package com.parkin.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ParkInLightColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = AccentBlue,
    onPrimaryContainer = Color.White,

    secondary = TextSecondary,
    onSecondary = Color.White,

    background = LightBg,
    onBackground = TextPrimary,

    surface = SurfaceWhite,
    onSurface = TextPrimary,

    outline = BorderGray
)

private val ParkInDarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = AccentBlue,
    onPrimaryContainer = Color.White,

    secondary = TextSecondary,
    onSecondary = Color.White,

    background = Color(0xFF121212),
    onBackground = Color.White,

    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,

    outline = Color(0xFF444444)
)

@Composable
fun ParkInTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        ParkInDarkColorScheme
    } else {
        ParkInLightColorScheme
    }

    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window

            window.statusBarColor = colorScheme.primary.toArgb()

            WindowCompat
                .getInsetsController(window, view)
                .isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}