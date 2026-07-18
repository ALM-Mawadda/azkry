package com.azkry.app.features.calendar.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import com.azkry.app.core.components.CenteredProgress
import com.azkry.app.core.components.ScreenHeader
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.features.calendar.models.HijriMonth
import com.azkry.app.features.calendar.models.HijriMonthGrid
import com.azkry.app.features.calendar.viewmodels.HijriCalendarViewModel

@Composable
fun HijriCalendarView(
    onBack: () -> Unit,
    viewModel: HijriCalendarViewModel = hiltViewModel(),
) {
    val month = viewModel.month.collectAsStateWithLifecycle()
    val hijriOffsetDays = viewModel.hijriOffsetDays.collectAsStateWithLifecycle()
    HijriCalendarContent(
        month = month.value,
        hijriOffsetDays = hijriOffsetDays.value,
        onBack = onBack,
        onPreviousMonth = viewModel::onPreviousMonth,
        onNextMonth = viewModel::onNextMonth,
        onBackToCurrentMonth = viewModel::onBackToCurrentMonth,
        onHijriOffsetSelected = viewModel::onHijriOffsetSelected,
    )
}

@Composable
fun HijriCalendarContent(
    month: HijriMonth?,
    onBack: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onBackToCurrentMonth: () -> Unit,
    hijriOffsetDays: Int = 0,
    onHijriOffsetSelected: (Int) -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.calendar_title),
            onBack = onBack,
        )

        if (month == null) {
            CenteredProgress()
            return@Column
        }

        Column(
            modifier = Modifier.padding(horizontal = AzkrySpacing.Md),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // In RTL the right arrow moves backwards in time.
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowRight,
                        contentDescription = null,
                        tint = AzkryTheme.colors.TextSecondary,
                    )
                }
                Text(
                    text = month.title,
                    style = AzkryTextStyles.Title2,
                    color = AzkryTheme.colors.TextPrimary,
                    modifier = Modifier.clickable(onClick = onBackToCurrentMonth),
                )
                IconButton(onClick = onNextMonth) {
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowLeft,
                        contentDescription = null,
                        tint = AzkryTheme.colors.TextSecondary,
                    )
                }
            }

            WeekdayHeaderRow()
            MonthGrid(month)

            HijriOffsetRow(
                selected = hijriOffsetDays,
                onSelected = onHijriOffsetSelected,
            )

            Text(
                text = stringResource(R.string.calendar_note),
                style = AzkryTextStyles.Footnote,
                color = AzkryTheme.colors.TextSecondary,
                modifier = Modifier.padding(vertical = AzkrySpacing.Md),
            )
        }
    }
}

@Composable
private fun HijriOffsetRow(
    selected: Int,
    onSelected: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm)) {
        Text(
            text = stringResource(R.string.hijri_offset_title),
            style = AzkryTextStyles.Title3,
            color = AzkryTheme.colors.TextPrimary,
            modifier = Modifier.padding(top = AzkrySpacing.Md),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
        ) {
            (-2..2).forEach { offset ->
                Surface(
                    onClick = { onSelected(offset) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(AzkryRadius.Pill),
                    color = if (offset == selected) {
                        AzkryTheme.colors.SurfaceCardStrong
                    } else {
                        AzkryTheme.colors.SurfaceCard
                    },
                    contentColor = AzkryTheme.colors.TextPrimary,
                    border = BorderStroke(
                        1.dp,
                        if (offset == selected) {
                            AzkryTheme.colors.AccentYellow
                        } else {
                            AzkryTheme.colors.BorderDefault
                        },
                    ),
                ) {
                    Text(
                        text = if (offset == 0) {
                            stringResource(R.string.hijri_offset_zero)
                        } else {
                            stringResource(R.string.hijri_offset_value, offset)
                        },
                        style = AzkryTextStyles.Caption,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = AzkrySpacing.Sm),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekdayHeaderRow() {
    val labels = listOf(
        R.string.weekday_sat,
        R.string.weekday_sun,
        R.string.weekday_mon,
        R.string.weekday_tue,
        R.string.weekday_wed,
        R.string.weekday_thu,
        R.string.weekday_fri,
    )
    Row(modifier = Modifier.fillMaxWidth()) {
        labels.forEach { labelRes ->
            Text(
                text = stringResource(labelRes),
                style = AzkryTextStyles.Caption,
                color = AzkryTheme.colors.TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MonthGrid(month: HijriMonth) {
    val cells: List<com.azkry.app.features.calendar.models.HijriDayCell?> =
        List(month.leadingBlanks) { null } + month.days

    Column(verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs)) {
        cells.chunked(7).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs),
            ) {
                week.forEach { cell ->
                    DayCell(cell = cell, modifier = Modifier.weight(1f))
                }
                repeat(7 - week.size) {
                    Box(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    cell: com.azkry.app.features.calendar.models.HijriDayCell?,
    modifier: Modifier = Modifier,
) {
    if (cell == null) {
        Box(modifier = modifier.aspectRatio(1f))
        return
    }
    Surface(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(AzkryRadius.Sm),
        color = if (cell.isToday) AzkryTheme.colors.SurfaceCardStrong else AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(
            1.dp,
            if (cell.isToday) AzkryTheme.colors.AccentYellow else AzkryTheme.colors.Divider,
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = cell.hijriDay.toString(),
                style = AzkryTextStyles.Callout,
                color = if (cell.isToday) AzkryTheme.colors.AccentYellow else AzkryTheme.colors.TextPrimary,
            )
            Text(
                text = cell.gregorianDay.toString(),
                style = AzkryTextStyles.Caption,
                color = AzkryTheme.colors.TextTertiary,
            )
        }
    }
}

@AzkryPreview
@Composable
private fun HijriCalendarContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        HijriCalendarContent(
            month = HijriMonthGrid.monthAtOffset(0, java.time.LocalDate.of(2026, 7, 18)),
            onBack = {},
            onPreviousMonth = {},
            onNextMonth = {},
            onBackToCurrentMonth = {},
        )
    }
}
