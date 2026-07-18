package com.azkry.app.core.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The app's full token set, resolved per appearance through
 * [LocalAzkryColors]. Property names are capitalized to match their
 * long-standing call sites (`AzkryTheme.colors.TextPrimary`).
 */
@Immutable
data class AzkryColorScheme(
    // Night header gradient (the starry sky stays dark in both themes —
    // it is the app's identity, not a surface).
    val Night900: Color,
    val Night700: Color,
    val Night500: Color,

    // Page surfaces (the ambient gradient and bands).
    val Slate900: Color,
    val Slate800: Color,
    val Slate700: Color,
    val Slate600: Color,

    val Neutral0: Color,

    val SurfaceCard: Color,
    val SurfaceCardStrong: Color,
    val SurfaceSheet: Color,
    val Divider: Color,
    val BorderDefault: Color,

    /** Mushaf backdrop: near-black at night, warm paper in light. */
    val MushafBackground: Color,

    val TextPrimary: Color,
    val TextSecondary: Color,
    val TextTertiary: Color,

    val AccentYellow: Color,
    val AccentGreen: Color,
    val AccentGreenWhatsApp: Color,
    val AccentBlue: Color,
    val AccentTeal: Color,

    val Success: Color,
    val Error: Color,
    val Warning: Color,

    val RingTrack: Color,
    val ShadowSpot: Color,
    val ShadowAmbient: Color,

    val isDark: Boolean,
)

val DarkAzkryColors = AzkryColorScheme(
    Night900 = Color(0xFF0D1130),
    Night700 = Color(0xFF1A2150),
    Night500 = Color(0xFF232B66),
    Slate900 = Color(0xFF16202A),
    Slate800 = Color(0xFF1C2732),
    Slate700 = Color(0xFF24303C),
    Slate600 = Color(0xFF2C3945),
    Neutral0 = Color(0xFFFFFFFF),
    SurfaceCard = Color(0x14FFFFFF),
    SurfaceCardStrong = Color(0x1FFFFFFF),
    SurfaceSheet = Color(0xFF191C20),
    Divider = Color(0x1AFFFFFF),
    BorderDefault = Color(0x12FFFFFF),
    MushafBackground = Color(0xFF0B0F14),
    TextPrimary = Color(0xFFF2F5F8),
    TextSecondary = Color(0xFF9FACBA),
    TextTertiary = Color(0xFF71808F),
    AccentYellow = Color(0xFFF5C84C),
    AccentGreen = Color(0xFF3ED078),
    AccentGreenWhatsApp = Color(0xFF25D366),
    AccentBlue = Color(0xFF4DA3FF),
    AccentTeal = Color(0xFF39C2C9),
    Success = Color(0xFF34C759),
    Error = Color(0xFFFF6B6B),
    Warning = Color(0xFFF5A623),
    RingTrack = Color(0x33FFFFFF),
    ShadowSpot = Color(0x33000000),
    ShadowAmbient = Color(0x1F000000),
    isDark = true,
)

val LightAzkryColors = AzkryColorScheme(
    // The starry header keeps its night identity even in light mode.
    Night900 = Color(0xFF0D1130),
    Night700 = Color(0xFF1A2150),
    Night500 = Color(0xFF232B66),
    Slate900 = Color(0xFFE7EDF5),
    Slate800 = Color(0xFFEFF3F8),
    Slate700 = Color(0xFFF7F9FC),
    Slate600 = Color(0xFFE3E9F0),
    Neutral0 = Color(0xFFFFFFFF),
    SurfaceCard = Color(0xFFFFFFFF),
    SurfaceCardStrong = Color(0xFFF0F4F9),
    SurfaceSheet = Color(0xFFFFFFFF),
    Divider = Color(0x14000000),
    BorderDefault = Color(0x14000000),
    MushafBackground = Color(0xFFFBF7EF),
    TextPrimary = Color(0xFF16202A),
    TextSecondary = Color(0xFF5A6B7C),
    TextTertiary = Color(0xFF8896A6),
    AccentYellow = Color(0xFFD99A1F),
    AccentGreen = Color(0xFF1F9D55),
    AccentGreenWhatsApp = Color(0xFF128C4B),
    AccentBlue = Color(0xFF1E7BE0),
    AccentTeal = Color(0xFF13929B),
    Success = Color(0xFF1F9D55),
    Error = Color(0xFFD64545),
    Warning = Color(0xFFC77F0A),
    RingTrack = Color(0x24000000),
    ShadowSpot = Color(0x24000000),
    ShadowAmbient = Color(0x14000000),
    isDark = false,
)

val LocalAzkryColors = staticCompositionLocalOf { DarkAzkryColors }
