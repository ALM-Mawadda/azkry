package com.azkry.app.core.preview

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.Preview

/**
 * The standard preview matrix used across the app.
 *
 * One `@AzkryPreview` annotation expands into three preview panels in
 * Android Studio: the canonical Arabic/RTL dark view, a small-device pass,
 * and a large font scale pass. The app is Arabic-first and dark-first, so
 * RTL/dark is the default canvas rather than a variant. This catches the
 * most common regression sources — token bypass, RTL layout bugs, and
 * Dynamic Type clipping — without bloating each composable with multiple
 * `@Preview` blocks.
 *
 * Always wrap the previewed composable in [AzkryPreviewSurface] so the
 * project's theme (colours, typography, spacing) is applied — Studio's
 * default preview canvas does not pull in `MaterialTheme`.
 */
@Preview(name = "Arabic", uiMode = UI_MODE_NIGHT_YES, locale = "ar", showBackground = true)
@Preview(
    name = "Small width",
    uiMode = UI_MODE_NIGHT_YES,
    locale = "ar",
    showBackground = true,
    widthDp = 320,
)
@Preview(
    name = "Large font",
    uiMode = UI_MODE_NIGHT_YES,
    locale = "ar",
    fontScale = 1.5f,
    showBackground = true,
)
annotation class AzkryPreview
