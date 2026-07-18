package com.azkry.app.core.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.azkry.app.core.theme.AzkryTheme

/**
 * Full-screen vertical gradient behind every screen: deep slate that darkens
 * towards the top so the starry night header on home blends into the page.
 */
@Composable
fun AmbientBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        AzkryTheme.colors.Slate900,
                        AzkryTheme.colors.Slate800,
                        AzkryTheme.colors.Slate700,
                    ),
                ),
            ),
    ) {
        content()
    }
}
