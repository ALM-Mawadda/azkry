package com.azkry.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable

/** Token accessor mirroring the MaterialTheme pattern: `AzkryTheme.colors.X`. */
object AzkryTheme {
    val colors: AzkryColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalAzkryColors.current
}

/**
 * Applies the app's palette and typography. The design is dark-first; the
 * light scheme exists behind the settings appearance toggle.
 */
@Composable
fun AzkryTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val palette = if (darkTheme) DarkAzkryColors else LightAzkryColors

    val materialScheme = if (darkTheme) {
        darkColorScheme(
            primary = palette.AccentYellow,
            onPrimary = palette.Night900,
            primaryContainer = palette.Night700,
            onPrimaryContainer = palette.TextPrimary,
            secondary = palette.AccentTeal,
            onSecondary = palette.Night900,
            secondaryContainer = palette.Slate700,
            onSecondaryContainer = palette.TextPrimary,
            tertiary = palette.AccentGreen,
            onTertiary = palette.Night900,
            background = palette.Slate800,
            onBackground = palette.TextPrimary,
            surface = palette.Slate700,
            onSurface = palette.TextPrimary,
            surfaceVariant = palette.Slate600,
            onSurfaceVariant = palette.TextSecondary,
            outline = palette.BorderDefault,
            outlineVariant = palette.Divider,
            error = palette.Error,
            onError = palette.Neutral0,
        )
    } else {
        lightColorScheme(
            primary = palette.AccentYellow,
            onPrimary = palette.Neutral0,
            primaryContainer = palette.Slate900,
            onPrimaryContainer = palette.TextPrimary,
            secondary = palette.AccentTeal,
            onSecondary = palette.Neutral0,
            secondaryContainer = palette.Slate600,
            onSecondaryContainer = palette.TextPrimary,
            tertiary = palette.AccentGreen,
            onTertiary = palette.Neutral0,
            background = palette.Slate800,
            onBackground = palette.TextPrimary,
            surface = palette.SurfaceCard,
            onSurface = palette.TextPrimary,
            surfaceVariant = palette.Slate600,
            onSurfaceVariant = palette.TextSecondary,
            outline = palette.BorderDefault,
            outlineVariant = palette.Divider,
            error = palette.Error,
            onError = palette.Neutral0,
        )
    }

    CompositionLocalProvider(LocalAzkryColors provides palette) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = AzkryTypography,
            content = content,
        )
    }
}
