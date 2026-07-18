package com.azkry.app.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

private val AzkryFontFamily = AzkryFonts.Almarai

private val AzkryPlatformStyle = PlatformTextStyle(includeFontPadding = false)

private val AzkryLineHeightStyle = LineHeightStyle(
    alignment = LineHeightStyle.Alignment.Center,
    trim = LineHeightStyle.Trim.None,
)

private fun azkryStyle(
    weight: FontWeight,
    size: TextUnit,
    lineHeight: TextUnit,
): TextStyle = TextStyle(
    fontFamily = AzkryFontFamily,
    fontWeight = weight,
    fontSize = size,
    lineHeight = lineHeight,
    platformStyle = AzkryPlatformStyle,
    lineHeightStyle = AzkryLineHeightStyle,
)

object AzkryTextStyles {
    val Display = azkryStyle(FontWeight.Bold, 34.sp, 43.sp)
    val Title1 = azkryStyle(FontWeight.Bold, 24.sp, 30.sp)
    val Title2 = azkryStyle(FontWeight.Bold, 20.sp, 26.sp)
    val Title3 = azkryStyle(FontWeight.SemiBold, 17.sp, 22.sp)
    val Headline = azkryStyle(FontWeight.SemiBold, 16.sp, 21.sp)
    val Body = azkryStyle(FontWeight.Normal, 15.sp, 20.sp)
    val BodyStrong = azkryStyle(FontWeight.SemiBold, 15.sp, 20.sp)
    val Callout = azkryStyle(FontWeight.SemiBold, 14.sp, 18.sp)
    val Subhead = azkryStyle(FontWeight.Normal, 14.sp, 18.sp)
    val Footnote = azkryStyle(FontWeight.Normal, 13.sp, 18.sp)
    val Label = azkryStyle(FontWeight.Medium, 12.sp, 16.sp)
    val Caption = azkryStyle(FontWeight.Medium, 11.sp, 14.sp)

    /** Adhkar body text: Naskh face, larger size, generous line height for tashkeel. */
    val DhikrBody = azkryStyle(FontWeight.Normal, 20.sp, 38.sp).copy(fontFamily = AzkryFonts.Amiri)
}

val AzkryTypography = Typography(
    displayLarge = AzkryTextStyles.Display,
    displaySmall = AzkryTextStyles.Display,
    headlineLarge = AzkryTextStyles.Title1,
    headlineMedium = AzkryTextStyles.Title1,
    headlineSmall = AzkryTextStyles.Title2,
    titleLarge = AzkryTextStyles.Title3,
    titleMedium = AzkryTextStyles.Headline,
    titleSmall = AzkryTextStyles.Callout,
    bodyLarge = AzkryTextStyles.Body,
    bodyMedium = AzkryTextStyles.Subhead,
    bodySmall = AzkryTextStyles.Footnote,
    labelLarge = AzkryTextStyles.Callout,
    labelMedium = AzkryTextStyles.Label,
    labelSmall = AzkryTextStyles.Caption,
)
