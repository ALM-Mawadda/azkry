package com.azkry.app.features.pages.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.IosShare
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.azkry.app.R
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.features.pages.models.PageSection

/**
 * Content block card for topic pages and the hub's inline dhikr: optional
 * heading row (with a share action for dhikr texts), the body — Amiri for
 * dhikr, plain for notes — then the explanatory note and source line.
 */
@Composable
fun PageSectionCard(
    section: PageSection,
    onShare: ((PageSection) -> Unit)? = null,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Card),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Column(
            modifier = Modifier.padding(AzkrySpacing.S20),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
        ) {
            if (section.heading != null || (section.isDhikr && onShare != null)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = section.heading.orEmpty(),
                        style = AzkryTextStyles.Title3,
                        modifier = Modifier.weight(1f),
                    )
                    if (section.isDhikr && onShare != null) {
                        IconButton(onClick = { onShare(section) }) {
                            Icon(
                                imageVector = Icons.Outlined.IosShare,
                                contentDescription = stringResource(R.string.action_share),
                                tint = AzkryTheme.colors.TextSecondary,
                                modifier = Modifier.size(20.dp),
                            )
                        }
                    }
                }
            }

            Text(
                text = section.body,
                style = if (section.isDhikr) {
                    AzkryTextStyles.DhikrBody
                } else {
                    AzkryTextStyles.Body.copy(lineHeight = 24.sp)
                },
                color = AzkryTheme.colors.TextPrimary,
            )

            if (section.note != null) {
                Text(
                    text = section.note,
                    style = AzkryTextStyles.Subhead,
                    color = AzkryTheme.colors.TextSecondary,
                )
            }

            if (section.source != null) {
                Text(
                    text = section.source,
                    style = AzkryTextStyles.Footnote,
                    color = AzkryTheme.colors.TextTertiary,
                )
            }
        }
    }
}

@AzkryPreview
@Composable
private fun PageSectionCardPreview() {
    AzkryPreviewSurface {
        PageSectionCard(
            section = PageSection(
                heading = "ركعتا الفجر",
                body = "رَكْعَتَا الْفَجْرِ خَيْرٌ مِنَ الدُّنْيَا وَمَا فِيهَا.",
                source = "رواه مسلم",
                isDhikr = true,
            ),
            onShare = {},
        )
    }
}
