package com.morphiclabs.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = MorphicGreen,
    secondary = MorphicGreenDark,
    background = DeepBlack,
    surface = SurfaceBlack,
    onPrimary = DeepBlack,
    onBackground = TextWhite,
    onSurface = TextWhite
)

@Composable
fun MorphicLabsAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desactivamos dynamicColor por defecto para mantener la identidad visual
    dynamicColor: Boolean = false, 
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme // Forzamos el esquema oscuro/Morphic

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DeepBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
