package com.azkry.app.features.home.views

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private class Star(
    val xFraction: Float,
    val yFraction: Float,
    val radiusFraction: Float,
    val alpha: Float,
)

// Deterministic star field: the same sky on every launch, denser and brighter
// toward the top so it fades into the page like the design's night header.
private val STARS: List<Star> = Random(1448).let { random ->
    List(56) {
        val y = random.nextFloat()
        Star(
            xFraction = random.nextFloat(),
            yFraction = y * y, // bias upward
            radiusFraction = 0.6f + random.nextFloat(),
            alpha = (0.18f + random.nextFloat() * 0.6f) * (1f - y * 0.6f),
        )
    }
}

/** Indices of stars that get a four-point sparkle. */
private val SPARKLES = setOf(7, 21, 40)

/**
 * Paints the design's signature scattered stars behind the content. Apply
 * after the gradient background and before padding so stars reach the edges.
 * [intensity] scales every star's alpha (fading stars at dawn/dusk).
 */
fun Modifier.starrySky(intensity: Float = 1f): Modifier = drawBehind {
    if (intensity <= 0f) return@drawBehind
    val starColor = Color(0xFFE8EEFF)
    STARS.forEachIndexed { index, star ->
        val center = Offset(star.xFraction * size.width, star.yFraction * size.height)
        val radius = star.radiusFraction * 1.2.dp.toPx()
        drawCircle(
            color = starColor,
            radius = radius,
            center = center,
            alpha = star.alpha * intensity,
        )
        if (index in SPARKLES) {
            val ray = radius * 4f
            val stroke = 0.8.dp.toPx()
            drawLine(
                color = starColor,
                start = center - Offset(0f, ray),
                end = center + Offset(0f, ray),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
                alpha = star.alpha * 0.7f * intensity,
            )
            drawLine(
                color = starColor,
                start = center - Offset(ray, 0f),
                end = center + Offset(ray, 0f),
                strokeWidth = stroke,
                cap = StrokeCap.Round,
                alpha = star.alpha * 0.7f * intensity,
            )
        }
    }
}
