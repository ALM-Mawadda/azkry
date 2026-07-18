package com.azkry.app.core.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowForward
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azkry.app.R
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles

/**
 * Standard sub-screen header: centered title with a circular back button on
 * the reading-start side (right in RTL, matching the design screenshots) and
 * optional action buttons on the opposite side.
 */
@Composable
fun ScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    actions: (@Composable () -> Unit)? = null,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.Sm),
    ) {
        // Deliberately not auto-mirrored: the design's back chevron points
        // right (toward the RTL reading start), where this button sits.
        CircleIconButton(
            icon = Icons.Outlined.ArrowForward,
            contentDescription = stringResource(R.string.action_back),
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterStart),
        )

        Text(
            text = title,
            style = AzkryTextStyles.Title2,
            color = AzkryTheme.colors.TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = AzkrySpacing.S40 + AzkrySpacing.Md),
        )

        if (actions != null) {
            Row(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                actions()
            }
        }
    }
}

@AzkryPreview
@Composable
private fun ScreenHeaderPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        ScreenHeader(title = "متابعة العبادات", onBack = {})
    }
}
