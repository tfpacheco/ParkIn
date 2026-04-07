package com.parkin.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Configuramos apenas o LightColorScheme para garantir que as tuas cores
// apareçam exatamente como definiste, independentemente do modo do telemóvel.
private val ParkInColorScheme = lightColorScheme(
    primary = AccentBlue,        // O teu azul de destaque
    onPrimary = Color.White,     // Texto branco sobre o azul
    primaryContainer = AccentBlue,
    onPrimaryContainer = Color.White,

    secondary = TextSecondary,   // Cinza para textos secundários
    onSecondary = Color.White,

    background = LightBg,        // O teu cinza muito claro de fundo
    onBackground = TextPrimary,  // Texto quase preto no fundo

    surface = SurfaceWhite,      // Branco para os cartões/inputs
    onSurface = TextPrimary,     // Texto quase preto sobre o branco

    outline = BorderGray         // Cor para as bordas dos inputs
)

@Composable
fun ParkInTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = ParkInColorScheme
    val view = LocalView.current

    // Isto ajusta a barra de estado (onde fica a bateria/hora) para combinar com a app
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // Usa as fontes padrão ou as que definires
        content = content
    )
}


