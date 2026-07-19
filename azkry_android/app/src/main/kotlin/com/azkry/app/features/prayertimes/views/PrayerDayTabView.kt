package com.azkry.app.features.prayertimes.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azkry.app.R
import androidx.compose.foundation.layout.size
import com.azkry.app.core.components.CenteredProgress
import com.azkry.app.core.components.prayerIcon
import com.azkry.app.core.models.Prayer
import com.azkry.app.core.models.labelRes
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.features.prayertimes.viewmodels.PrayerDayUiState
import com.azkry.app.features.prayertimes.viewmodels.PrayerDayViewModel
import com.azkry.app.features.prayertimes.viewmodels.PrayerTimeRow

/** The الصلاة home tab: full prayer times for a navigable day. */
@Composable
fun PrayerDayTabView(
    viewModel: PrayerDayViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    PrayerDayTabContent(
        state = state.value,
        onPreviousDay = viewModel::onPreviousDay,
        onNextDay = viewModel::onNextDay,
        onBackToToday = viewModel::onBackToToday,
    )
}

@Composable
fun PrayerDayTabContent(
    state: PrayerDayUiState?,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onBackToToday: () -> Unit,
) {
    if (state == null) {
        CenteredProgress(modifier = Modifier.padding(vertical = AzkrySpacing.Xl))
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.S12),
        verticalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // In RTL the right arrow moves backwards in time (reading order).
            IconButton(onClick = onPreviousDay) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.action_previous_day),
                    tint = AzkryTheme.colors.TextSecondary,
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = state.hijriLabel,
                    style = AzkryTextStyles.Title3,
                    color = AzkryTheme.colors.TextPrimary,
                )
                Text(
                    text = "${state.dateKey} · ${state.cityName}",
                    style = AzkryTextStyles.Footnote,
                    color = AzkryTheme.colors.TextSecondary,
                )
                if (!state.isToday) {
                    Text(
                        text = stringResource(R.string.prayer_back_to_today),
                        style = AzkryTextStyles.Callout,
                        color = AzkryTheme.colors.AccentBlue,
                        modifier = Modifier
                            .padding(top = AzkrySpacing.Xs)
                            .clickable(onClick = onBackToToday),
                    )
                }
            }
            IconButton(onClick = onNextDay) {
                Icon(
                    imageVector = Icons.Outlined.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.action_next_day),
                    tint = AzkryTheme.colors.TextSecondary,
                )
            }
        }

        state.rows.forEach { row ->
            PrayerTimeRowCard(row)
        }
    }
}

@Composable
private fun PrayerTimeRowCard(row: PrayerTimeRow) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Lg),
        color = if (row.isNext) AzkryTheme.colors.SurfaceCardStrong else AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(
            1.dp,
            if (row.isNext) AzkryTheme.colors.AccentYellow else AzkryTheme.colors.BorderDefault,
        ),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = AzkrySpacing.Md,
                vertical = AzkrySpacing.Md,
            ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = prayerIcon(row.prayer),
                    contentDescription = null,
                    tint = if (row.isNext) AzkryTheme.colors.AccentYellow else AzkryTheme.colors.TextSecondary,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = stringResource(row.prayer.labelRes()),
                    style = AzkryTextStyles.Headline,
                )
            }
            Text(
                text = row.time,
                style = AzkryTextStyles.Headline,
                color = if (row.isNext) AzkryTheme.colors.AccentYellow else AzkryTheme.colors.TextSecondary,
            )
        }
    }
}

@AzkryPreview
@Composable
private fun PrayerDayTabContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        PrayerDayTabContent(
            state = PrayerDayUiState(
                dateKey = "2026-07-18",
                hijriLabel = "4 صفر",
                cityName = "مكة المكرمة",
                isToday = true,
                rows = Prayer.entries.mapIndexed { index, prayer ->
                    PrayerTimeRow(prayer = prayer, time = "4:15", isNext = index == 4)
                },
            ),
            onPreviousDay = {},
            onNextDay = {},
            onBackToToday = {},
        )
    }
}
