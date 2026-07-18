package com.azkry.app.core.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles

/**
 * Glassy list-row card with a rounded icon tile on the reading-start side, a
 * title (plus optional subtitle), and a forward chevron — the primary
 * navigation row across home, adhkar categories, and settings pages.
 *
 * The chevron auto-mirrors, so in the app's RTL layout it points left as in
 * the design screenshots.
 */
@Composable
fun SectionRowCard(
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    iconTint: Color = AzkryTheme.colors.TextPrimary,
    iconContainerColor: Color = AzkryTheme.colors.Slate900,
    trailing: (@Composable () -> Unit)? = null,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Card),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AzkrySpacing.Md,
                vertical = AzkrySpacing.S20,
            ),
            horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (icon != null) {
                Surface(
                    modifier = Modifier.size(44.dp),
                    shape = RoundedCornerShape(AzkryRadius.Md),
                    color = iconContainerColor,
                    contentColor = iconTint,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp),
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs),
            ) {
                Text(
                    text = title,
                    style = AzkryTextStyles.Headline,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = AzkryTextStyles.Subhead,
                        color = AzkryTheme.colors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            if (trailing != null) {
                trailing()
            }

            // Deliberately not auto-mirrored: the design's forward chevron
            // points left, which matches this RTL-only app's reading flow.
            Icon(
                imageVector = Icons.Outlined.KeyboardArrowLeft,
                contentDescription = null,
                tint = AzkryTheme.colors.TextTertiary,
            )
        }
    }
}

@AzkryPreview
@Composable
private fun SectionRowCardPreview() {
    AzkryPreviewSurface {
        SectionRowCard(
            title = "الأذكار والأدعية",
            subtitle = "يوم الجمعة",
            icon = Icons.Outlined.AutoAwesome,
            onClick = {},
        )
    }
}
