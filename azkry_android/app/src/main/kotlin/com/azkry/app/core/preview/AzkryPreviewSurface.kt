package com.azkry.app.core.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.azkry.app.core.theme.AzkryTheme

/**
 * Default wrapper for `@AzkryPreview` blocks.
 *
 * Applies [AzkryTheme] so design tokens resolve, paints the page background
 * colour the app uses (`AzkryTheme.colors.Slate800`), and adds 16dp of outer
 * padding so the previewed component does not sit flush with the canvas
 * edge. Pass `padding = 0.dp` for full-bleed components like sheets or
 * headers.
 */
@Composable
fun AzkryPreviewSurface(
    padding: Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    AzkryTheme {
        Surface(color = AzkryTheme.colors.Slate800) {
            Box(modifier = Modifier.padding(padding)) {
                content()
            }
        }
    }
}
