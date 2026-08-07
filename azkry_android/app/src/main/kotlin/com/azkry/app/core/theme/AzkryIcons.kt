package com.azkry.app.core.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Directional glyphs for this RTL-only app.
 *
 * The design's forward chevron points **left** and its back chevron points
 * **right**, so the auto-mirrored Material icons are deliberately not used —
 * they would flip these back under RTL. Centralised here so the single
 * deprecation suppression carries that reason for every screen instead of
 * being repeated (and eventually mis-copied) at each call site.
 */
@Suppress("DEPRECATION")
object AzkryIcons {
    /** Moves the reader onward — points left in this layout. */
    val Forward: ImageVector get() = Icons.Outlined.KeyboardArrowLeft

    /** Returns the reader — points right in this layout. */
    val Back: ImageVector get() = Icons.Outlined.KeyboardArrowRight

    /** The header back arrow, likewise pointing right. */
    val BackArrow: ImageVector get() = Icons.Outlined.ArrowForward
}
