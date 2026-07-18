package com.azkry.app.core.theme

import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.sp
import com.azkry.app.R

/**
 * Bundled Arabic fonts (all SIL OFL). Almarai is the rounded UI face carrying
 * the whole chrome — the design's identity — while the Amiri Naskh faces
 * carry religious body text where diacritics need a traditional hand.
 */
object AzkryFonts {
    /** Rounded Arabic UI face used across all chrome text. */
    val Almarai = FontFamily(
        Font(R.font.almarai_light, FontWeight.Light),
        Font(R.font.almarai, FontWeight.Normal),
        Font(R.font.almarai, FontWeight.Medium),
        Font(R.font.almarai_bold, FontWeight.SemiBold),
        Font(R.font.almarai_bold, FontWeight.Bold),
        Font(R.font.almarai_extrabold, FontWeight.ExtraBold),
    )

    /** General Naskh for adhkar and dua body text. */
    val Amiri = FontFamily(Font(R.font.amiri))

    /** Quran-specific face with the full Quranic mark repertoire. */
    val AmiriQuran = FontFamily(Font(R.font.amiri_quran))
}

/** Mushaf body text: generous line height for stacked Quranic marks. */
val QuranTextStyle = TextStyle(
    fontFamily = AzkryFonts.AmiriQuran,
    fontWeight = FontWeight.Normal,
    fontSize = 24.sp,
    lineHeight = 52.sp,
    platformStyle = PlatformTextStyle(includeFontPadding = false),
    lineHeightStyle = LineHeightStyle(
        alignment = LineHeightStyle.Alignment.Center,
        trim = LineHeightStyle.Trim.None,
    ),
)
