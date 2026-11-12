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
import com.example.ui.settings.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary = PilgrimYellow,
    onPrimary = CaminoBlueDark,
    primaryContainer = CaminoBlue,
    onPrimaryContainer = Color.White,
    secondary = PilgrimYellow,
    onSecondary = Color.Black,
    secondaryContainer = CaminoBlueDark,
    onSecondaryContainer = PilgrimYellow,
    tertiary = CaminoGreen,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = DarkOnSurface,
    onSurface = DarkOnSurface
)

private val LightColorScheme = lightColorScheme(
    primary = CaminoBlue,
    onPrimary = Color.White,
    primaryContainer = CaminoBlueContainer,
    onPrimaryContainer = CaminoBlueDark,
    secondary = PilgrimYellowDark,
    onSecondary = Color.White,
    secondaryContainer = PilgrimYellowLight,
    onSecondaryContainer = CaminoBlueDark,
    tertiary = CaminoGreen,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = LightOnSurface,
    onSurface = LightOnSurface
)

@Composable
fun UltreiaTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false, // Preserve brand Camino colors by default
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val darkTheme = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

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
