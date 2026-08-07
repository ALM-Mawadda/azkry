package com.azkry.app.features.prayertimes.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azkry.app.R
import com.azkry.app.core.components.CenteredProgress
import com.azkry.app.core.components.ScreenHeader
import com.azkry.app.core.components.prayerIcon
import com.azkry.app.core.models.Prayer
import com.azkry.app.core.models.labelRes
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.theme.AzkryFonts
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.features.prayertimes.models.ForbiddenWindow
import com.azkry.app.features.prayertimes.viewmodels.PrayerDayUiState
import com.azkry.app.features.prayertimes.viewmodels.PrayerDayViewModel
import com.azkry.app.features.prayertimes.viewmodels.PrayerTimeRow

/**
 * The الصلاة screen: a dedicated full-screen prayer board, like the
 * reference — its own city header, no home chrome behind it.
 */
@Composable
fun PrayerDayScreen(
    onBack: () -> Unit,
    onOpenForbiddenTimes: () -> Unit,
    viewModel: PrayerDayViewModel = hiltViewModel(),
) {
    // The shell has no nav graph, so this ViewModel is activity-scoped and
    // outlives the screen. Re-opening الصلاة should land on today; the flag
    // survives rotation but not leaving the screen, so a stepped-to day is
    // only kept for the visit that chose it.
    var visited by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!visited) viewModel.onBackToToday()
        visited = true
    }

    val state = viewModel.state.collectAsStateWithLifecycle()
    PrayerDayContent(
        state = state.value,
        onBack = onBack,
        onPreviousDay = viewModel::onPreviousDay,
        onNextDay = viewModel::onNextDay,
        onBackToToday = viewModel::onBackToToday,
        onOpenForbiddenTimes = onOpenForbiddenTimes,
    )
}

@Composable
fun PrayerDayContent(
    state: PrayerDayUiState?,
    onBack: () -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onBackToToday: () -> Unit,
    onOpenForbiddenTimes: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = state?.cityName.orEmpty(),
            onBack = onBack,
            actions = {
                // Day stepping lives beside the title so the board itself
                // stays as clean as the reference.
                IconButton(onClick = onNextDay) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.action_next_day),
                        tint = AzkryTheme.colors.TextSecondary,
                    )
                }
                IconButton(onClick = onPreviousDay) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.action_previous_day),
                        tint = AzkryTheme.colors.TextSecondary,
                    )
                }
            },
        )

        if (state == null) {
            CenteredProgress(modifier = Modifier.padding(vertical = AzkrySpacing.Xl))
            return@Column
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.S12)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
        ) {
            if (!state.isToday) {
                Text(
                    text = stringResource(R.string.prayer_back_to_today),
                    style = AzkryTextStyles.Callout,
                    color = AzkryTheme.colors.AccentBlue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onBackToToday),
                    textAlign = TextAlign.Center,
                )
            }
            CalligraphyCard(state)
            DatePills(state)
            NextPrayerCard(state = state, onOpenForbiddenTimes = onOpenForbiddenTimes)
            PrayerRowsCard(state.rows)
        }
    }
}

/** Weekday and hijri month in Amiri, flanking a crescent. */
@Composable
private fun CalligraphyCard(state: PrayerDayUiState) {
    PrayerCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AzkrySpacing.Lg, vertical = AzkrySpacing.Md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CalligraphyText(state.dayName, modifier = Modifier.weight(1f))
            Icon(
                imageVector = Icons.Outlined.DarkMode,
                contentDescription = null,
                tint = AzkryTheme.colors.TextPrimary,
                modifier = Modifier.size(30.dp),
            )
            CalligraphyText(state.hijriMonthName, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun CalligraphyText(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = AzkryTextStyles.Display.copy(fontFamily = AzkryFonts.Amiri),
        color = AzkryTheme.colors.TextPrimary,
        textAlign = TextAlign.Center,
        modifier = modifier,
    )
}

/** Hijri and gregorian ISO dates, side by side. */
@Composable
private fun DatePills(state: PrayerDayUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
    ) {
        DatePill(text = state.hijriIso, modifier = Modifier.weight(1f))
        DatePill(text = state.dateKey, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DatePill(text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(AzkryRadius.Pill),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Text(
            text = text,
            style = AzkryTextStyles.Title3,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AzkrySpacing.S12),
        )
    }
}

/**
 * The next adhan with its live countdown, and — fused below a divider — the
 * current forbidden-prayer window when one is open.
 */
@Composable
private fun NextPrayerCard(
    state: PrayerDayUiState,
    onOpenForbiddenTimes: () -> Unit,
) {
    // Stepping to another day leaves nothing to show here; an empty card would
    // still stroke its border and eat a row of spacing.
    if (state.nextPrayer == null && state.forbiddenWindow == null) return

    PrayerCard {
        Column {
            if (state.nextPrayer != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.Md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs)) {
                        Text(
                            text = stringResource(R.string.prayer_next_label),
                            style = AzkryTextStyles.Callout,
                            color = AzkryTheme.colors.AccentYellow,
                        )
                        Text(
                            text = stringResource(state.nextPrayer.labelRes()),
                            style = AzkryTextStyles.Title1,
                            color = AzkryTheme.colors.TextPrimary,
                        )
                    }

                    PrayerGlyph(prayer = state.nextPrayer, highlighted = true)

                    Column(
                        horizontalAlignment = Alignment.End,
                        verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs),
                    ) {
                        TimeWithMeridiem(
                            time = state.nextPrayerTime,
                            isAm = state.nextPrayerIsAm,
                            timeStyle = AzkryTextStyles.Title1,
                        )
                        Text(
                            text = state.countdown,
                            style = AzkryTextStyles.Title2,
                            color = AzkryTheme.colors.AccentYellow,
                        )
                    }
                }
            }

            if (state.forbiddenWindow != null) {
                if (state.nextPrayer != null) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = AzkryTheme.colors.BorderDefault,
                    )
                }
                ForbiddenTimesRow(
                    window = state.forbiddenWindow,
                    onClick = onOpenForbiddenTimes,
                )
            }
        }
    }
}

@Composable
private fun ForbiddenTimesRow(window: ForbiddenWindow, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.Md),
        horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 44.dp)
                .background(AzkryTheme.colors.Error, RoundedCornerShape(2.dp)),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs),
        ) {
            Text(
                text = stringResource(R.string.prayer_forbidden_title),
                style = AzkryTextStyles.Headline,
                color = AzkryTheme.colors.TextPrimary,
            )
            Text(
                text = stringResource(window.descriptionRes()),
                style = AzkryTextStyles.Subhead,
                color = AzkryTheme.colors.TextSecondary,
            )
        }
        // Deliberately not auto-mirrored: the design's forward chevron points
        // left, matching this RTL-only app.
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowLeft,
            contentDescription = null,
            tint = AzkryTheme.colors.TextTertiary,
        )
    }
}

/** All six times in one grouped card, divided like the reference. */
@Composable
private fun PrayerRowsCard(rows: List<PrayerTimeRow>) {
    PrayerCard {
        Column {
            rows.forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = AzkryTheme.colors.BorderDefault,
                    )
                }
                PrayerTimeRowItem(row)
            }
        }
    }
}

@Composable
private fun PrayerTimeRowItem(row: PrayerTimeRow) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (row.isNext) AzkryTheme.colors.SurfaceCardStrong else Color.Transparent,
            )
            .padding(horizontal = AzkrySpacing.Md, vertical = AzkrySpacing.S12),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(row.prayer.labelRes()),
            style = AzkryTextStyles.Title2,
            color = AzkryTheme.colors.TextPrimary,
            modifier = Modifier.weight(1f),
        )

        PrayerGlyph(prayer = row.prayer, highlighted = row.isNext)

        // End alignment keeps the time at the far edge (visual left in RTL)
        // instead of crowding the glyph.
        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs),
        ) {
            TimeWithMeridiem(
                time = row.time,
                isAm = row.isAm,
                timeStyle = AzkryTextStyles.Title2,
            )
            // Only Ishraq has a second line: it follows from the calculated
            // sunrise. Iqama is the mosque's to set, so the app never states it.
            if (row.ishraqTime != null) {
                Text(
                    text = stringResource(R.string.prayer_ishraq_line, row.ishraqTime),
                    style = AzkryTextStyles.Footnote,
                    color = AzkryTheme.colors.TextTertiary,
                )
            }
        }
    }
}

/** A time and its ص/م marker — the screen renders every clock time this way. */
@Composable
private fun TimeWithMeridiem(
    time: String,
    isAm: Boolean,
    timeStyle: TextStyle,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = time,
            style = timeStyle,
            color = AzkryTheme.colors.TextPrimary,
        )
        Text(
            text = stringResource(if (isAm) R.string.prayer_am else R.string.prayer_pm),
            style = AzkryTextStyles.Title3,
            color = AzkryTheme.colors.TextPrimary,
        )
    }
}

/** The dim circular time-of-day glyph; gold on the next prayer. */
@Composable
private fun PrayerGlyph(prayer: Prayer, highlighted: Boolean) {
    Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = if (highlighted) {
            AzkryTheme.colors.AccentYellow.copy(alpha = 0.15f)
        } else {
            AzkryTheme.colors.Slate900.copy(alpha = 0.5f)
        },
        contentColor = if (highlighted) {
            AzkryTheme.colors.AccentYellow
        } else {
            AzkryTheme.colors.TextTertiary
        },
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = prayerIcon(prayer),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
            )
        }
    }
}

@Composable
private fun PrayerCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Card),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        content()
    }
}

private fun ForbiddenWindow.descriptionRes(): Int = when (this) {
    ForbiddenWindow.AfterFajr -> R.string.prayer_forbidden_after_fajr
    ForbiddenWindow.Zawal -> R.string.prayer_forbidden_zawal
    ForbiddenWindow.AfterAsr -> R.string.prayer_forbidden_after_asr
}

@AzkryPreview
@Composable
private fun PrayerDayContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        PrayerDayContent(
            state = PrayerDayUiState(
                dateKey = "2026-07-19",
                hijriIso = "1448-02-05",
                hijriMonthName = "صفر",
                dayName = "الأحد",
                cityName = "مكة المكرمة",
                isToday = true,
                nextPrayer = Prayer.Maghrib,
                nextPrayerTime = "9:22",
                nextPrayerIsAm = false,
                countdown = "1:46:54",
                forbiddenWindow = ForbiddenWindow.AfterAsr,
                rows = Prayer.entries.map { prayer ->
                    PrayerTimeRow(
                        prayer = prayer,
                        time = "4:19",
                        isAm = prayer == Prayer.Fajr || prayer == Prayer.Sunrise,
                        ishraqTime = "6:23".takeIf { prayer == Prayer.Sunrise },
                        isNext = prayer == Prayer.Maghrib,
                    )
                },
            ),
            onBack = {},
            onPreviousDay = {},
            onNextDay = {},
            onBackToToday = {},
            onOpenForbiddenTimes = {},
        )
    }
}
