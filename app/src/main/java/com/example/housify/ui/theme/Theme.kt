package com.example.housify.ui.theme

import androidx.compose.ui.graphics.Color
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = DarkBlue,
    secondary = LightGrey,
    tertiary = MidGrey,
    background = DarkModeDarkGrey,
    onPrimary = DarkModeLightGrey,
    onSecondary = DarkModeLightGrey,
    onBackground = Color.White,
    surface = DarkModeLightGrey,
    onSurface = Color.White,
    onSurfaceVariant = LightGrey,
    outline = LightGrey,
    inverseOnSurface = LightBlue,
    secondaryContainer = DarkModeLightGrey,
)

private val LightColorScheme = lightColorScheme(
    primary = DarkBlue,
    secondary = LightGrey,
    tertiary = MidGrey,
    background = LightBlue,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = DarkGrey,
    surface = Color.White,
    onSurface = DarkGrey,
    onSurfaceVariant = LightGrey,
    outline = LightGrey,
    inverseOnSurface = DarkBlue,
    secondaryContainer = Color.White,
)

@Composable
fun HousifyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
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