package com.azkry.app.features.home.views

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.azkry.app.features.home.models.HeaderPhase

/** The header's sky per phase: gradient stops top→bottom plus decorations. */
internal data class SkyPalette(
    val top: Color,
    val middle: Color,
    val bottom: Color,
    val starIntensity: Float,
    /** Null = no sun; otherwise the glow color and its vertical center fraction. */
    val sunColor: Color?,
    val sunCenterYFraction: Float,
)

internal fun skyPaletteFor(phase: HeaderPhase): SkyPalette = when (phase) {
    HeaderPhase.Night -> SkyPalette(
        top = Color(0xFF0D1130),
        middle = Color(0xFF1A2150),
        bottom = Color(0xFF232B52),
        starIntensity = 1f,
        sunColor = null,
        sunCenterYFraction = 0f,
    )

    HeaderPhase.Dawn -> SkyPalette(
        top = Color(0xFF141A3E),
        middle = Color(0xFF3A4176),
        bottom = Color(0xFF9A6647),
        starIntensity = 0.35f,
        sunColor = Color(0xFFFFC98A),
        sunCenterYFraction = 0.95f,
    )

    HeaderPhase.Day -> SkyPalette(
        top = Color(0xFF3E6FB4),
        middle = Color(0xFF5C8AC6),
        bottom = Color(0xFF7FA3D2),
        starIntensity = 0f,
        sunColor = Color(0xFFFFF3C4),
        sunCenterYFraction = 0.05f,
    )

    HeaderPhase.Afternoon -> SkyPalette(
        top = Color(0xFF41699E),
        middle = Color(0xFF6C8BB4),
        bottom = Color(0xFFC5A06A),
        starIntensity = 0f,
        sunColor = Color(0xFFFFE0A0),
        sunCenterYFraction = 0.65f,
    )

    HeaderPhase.Dusk -> SkyPalette(
        top = Color(0xFF262450),
        middle = Color(0xFF63406C),
        bottom = Color(0xFFB86A3E),
        starIntensity = 0.25f,
        sunColor = Color(0xFFFF9E66),
        sunCenterYFraction = 0.98f,
    )
}

/**
 * Paints the phase's sky: gradient, optional sun glow, stars, and a fade
 * into [pageColor] at the bottom so the header melts into the page.
 */
internal fun Modifier.sky(phase: HeaderPhase, pageColor: Color): Modifier {
    val palette = skyPaletteFor(phase)
    return drawBehind {
        drawRect(
            Brush.verticalGradient(
                colors = listOf(palette.top, palette.middle, palette.bottom),
            ),
        )
        palette.sunColor?.let { sun ->
            drawRect(
                Brush.radialGradient(
                    colors = listOf(sun.copy(alpha = 0.55f), sun.copy(alpha = 0f)),
                    center = Offset(size.width * 0.5f, size.height * palette.sunCenterYFraction),
                    radius = size.width * 0.55f,
                ),
            )
        }
        drawRect(
            Brush.verticalGradient(
                0f to Color.Transparent,
                0.72f to Color.Transparent,
                1f to pageColor,
            ),
        )
    }.starrySky(intensity = palette.starIntensity)
}
