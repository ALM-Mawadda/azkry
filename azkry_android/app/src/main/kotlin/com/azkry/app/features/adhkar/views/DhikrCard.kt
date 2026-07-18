package com.azkry.app.features.adhkar.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.azkry.app.R
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.preview.Samples
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.features.adhkar.viewmodels.DhikrCounterItem

/**
 * One dhikr with its tap counter: tap the card to count, long-press the
 * counter to reset, star to favorite. Shared by the reader and the
 * favorites tab.
 */
@Composable
fun DhikrCard(
    item: DhikrCounterItem,
    onTap: () -> Unit,
    onCounterLongPress: () -> Unit,
    onFavoriteToggle: () -> Unit,
    modifier: Modifier = Modifier,
    positionLabel: String? = null,
    onLongPress: (() -> Unit)? = null,
) {
    val haptics = LocalHapticFeedback.current
    val border = if (item.isComplete) {
        BorderStroke(1.dp, AzkryTheme.colors.AccentGreen)
    } else if (item.count > 0) {
        BorderStroke(1.dp, AzkryTheme.colors.AccentYellow)
    } else {
        BorderStroke(1.dp, AzkryTheme.colors.BorderDefault)
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.Confirm)
                    onTap()
                },
                onLongClick = onLongPress,
            ),
        shape = RoundedCornerShape(AzkryRadius.Card),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = border,
    ) {
        Column(
            modifier = Modifier.padding(AzkrySpacing.S20),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Md),
        ) {
            item.dhikr.title?.let { title ->
                Text(
                    text = title,
                    style = AzkryTextStyles.Title3,
                    textAlign = TextAlign.Center,
                    color = AzkryTheme.colors.AccentYellow,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Text(
                text = item.dhikr.text,
                style = AzkryTextStyles.DhikrBody,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            item.dhikr.virtue?.let { virtue ->
                Text(
                    text = virtue,
                    style = AzkryTextStyles.Subhead,
                    textAlign = TextAlign.Center,
                    color = AzkryTheme.colors.TextSecondary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = if (item.isFavorite) Icons.Outlined.Star else Icons.Outlined.StarBorder,
                        contentDescription = stringResource(R.string.tab_favorites),
                        tint = if (item.isFavorite) AzkryTheme.colors.AccentYellow else AzkryTheme.colors.TextTertiary,
                        modifier = Modifier
                            .size(24.dp)
                            .combinedClickable(onClick = onFavoriteToggle),
                    )
                    if (positionLabel != null) {
                        Text(
                            text = positionLabel,
                            style = AzkryTextStyles.Callout,
                            color = AzkryTheme.colors.TextSecondary,
                        )
                    }
                    item.dhikr.source?.let { source ->
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.MenuBook,
                            contentDescription = null,
                            tint = AzkryTheme.colors.TextSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = source,
                            style = AzkryTextStyles.Callout,
                            color = AzkryTheme.colors.TextSecondary,
                        )
                    }
                }

                DhikrCounterBadge(
                    item = item,
                    onLongPress = onCounterLongPress,
                )
            }
        }
    }
}

@Composable
private fun DhikrCounterBadge(
    item: DhikrCounterItem,
    onLongPress: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .size(48.dp)
            .combinedClickable(onClick = {}, onLongClick = onLongPress),
        shape = CircleShape,
        color = if (item.isComplete) AzkryTheme.colors.AccentGreen else AzkryTheme.colors.SurfaceCardStrong,
        contentColor = if (item.isComplete) AzkryTheme.colors.Night900 else AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (item.isComplete) {
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                )
            } else {
                Text(
                    text = item.remaining.toString(),
                    style = AzkryTextStyles.Callout,
                )
            }
        }
    }
}

@AzkryPreview
@Composable
private fun DhikrCardPreview() {
    AzkryPreviewSurface {
        DhikrCard(
            item = DhikrCounterItem(dhikr = Samples.tasbihDhikr, count = 40, isFavorite = true),
            onTap = {},
            onCounterLongPress = {},
            onFavoriteToggle = {},
            positionLabel = "2 | 25",
        )
    }
}
