package com.azkry.app.features.tracking.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.azkry.app.R
import com.azkry.app.core.components.CenteredProgress
import com.azkry.app.core.components.ProgressRing
import com.azkry.app.core.components.ScreenHeader
import com.azkry.app.core.components.prayerIcon
import com.azkry.app.core.models.Prayer
import com.azkry.app.core.models.labelRes
import com.azkry.app.core.preview.AzkryPreview
import com.azkry.app.core.preview.AzkryPreviewSurface
import com.azkry.app.core.theme.AzkryTheme
import com.azkry.app.core.theme.AzkryRadius
import com.azkry.app.core.theme.AzkrySpacing
import com.azkry.app.core.theme.AzkryTextStyles
import com.azkry.app.features.tracking.models.WorshipSection
import com.azkry.app.features.tracking.models.WorshipTask
import com.azkry.app.features.tracking.services.AdhkarCategoryProgress
import com.azkry.app.features.tracking.viewmodels.PrayerCell
import com.azkry.app.features.tracking.viewmodels.TrackingUiState
import com.azkry.app.features.tracking.viewmodels.TrackingViewModel
import com.azkry.app.features.tracking.viewmodels.WeekDayRing

@Composable
fun TrackingView(
    onBack: () -> Unit,
    viewModel: TrackingViewModel = hiltViewModel(),
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    TrackingContent(
        state = state.value,
        onBack = onBack,
        onPrayerToggled = viewModel::onPrayerToggled,
        onTaskToggled = viewModel::onTaskToggled,
    )
}

@Composable
fun TrackingContent(
    state: TrackingUiState?,
    onBack: () -> Unit,
    onPrayerToggled: (PrayerCell) -> Unit,
    onTaskToggled: (WorshipTask, Boolean) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        ScreenHeader(
            title = stringResource(R.string.tracking_title),
            onBack = onBack,
        )

        if (state == null) {
            CenteredProgress()
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = AzkrySpacing.Md,
                end = AzkrySpacing.Md,
                top = AzkrySpacing.Sm,
                bottom = AzkrySpacing.Xl,
            ),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
        ) {
            item {
                WeekCard(state)
            }

            item {
                SectionTitle(
                    title = stringResource(R.string.tracking_section_prayers),
                    counter = "${state.prayers.count { it.completed }}/${state.prayers.size}",
                )
            }
            item {
                PrayersRow(prayers = state.prayers, onPrayerToggled = onPrayerToggled)
            }

            item {
                SectionTitle(
                    title = stringResource(R.string.tracking_section_adhkar),
                    counter = "${state.adhkarRows.count { it.isCompleted }}/${state.adhkarRows.size}",
                )
            }
            state.adhkarRows.forEach { row ->
                item(key = "adhkar-${row.categoryId}") {
                    AdhkarProgressRow(row)
                }
            }

            taskSection(
                titleRes = R.string.tracking_section_quran,
                section = WorshipSection.Quran,
                state = state,
                onTaskToggled = onTaskToggled,
            )
            taskSection(
                titleRes = R.string.tracking_section_daily_worships,
                section = WorshipSection.Daily,
                state = state,
                onTaskToggled = onTaskToggled,
            )
            taskSection(
                titleRes = R.string.tracking_section_rawatib,
                section = WorshipSection.Rawatib,
                state = state,
                onTaskToggled = onTaskToggled,
            )

            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.taskSection(
    titleRes: Int,
    section: WorshipSection,
    state: TrackingUiState,
    onTaskToggled: (WorshipTask, Boolean) -> Unit,
) {
    val tasks = WorshipTask.entries.filter { it.section == section }
    item(key = "section-$section") {
        SectionTitle(
            title = stringResource(titleRes),
            counter = "${tasks.count { it in state.completedTasks }}/${tasks.size}",
        )
    }
    tasks.forEach { task ->
        item(key = "task-${task.name}") {
            val completed = task in state.completedTasks
            TaskRow(
                title = stringResource(task.titleRes),
                subtitle = stringResource(task.subtitleRes),
                completed = completed,
                onClick = { onTaskToggled(task, completed) },
            )
        }
    }
}

@Composable
private fun WeekCard(state: TrackingUiState) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Card),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(1.dp, AzkryTheme.colors.BorderDefault),
    ) {
        Column(
            modifier = Modifier.padding(AzkrySpacing.Md),
            verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Md),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs)) {
                    Text(
                        text = state.weekDays.firstOrNull { it.isToday }?.dayLabel.orEmpty(),
                        style = AzkryTextStyles.Title2,
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = AzkryTheme.colors.TextSecondary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "${state.hijriDate} — ${state.gregorianDate}",
                            style = AzkryTextStyles.Subhead,
                            color = AzkryTheme.colors.TextSecondary,
                        )
                    }
                }
                ProgressRing(
                    progress = state.todayPercent / 100f,
                    label = "${state.todayPercent}%",
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                state.weekDays.forEach { day ->
                    WeekDayCell(day)
                }
            }
        }
    }
}

@Composable
private fun WeekDayCell(day: WeekDayRing) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs),
    ) {
        Text(
            text = day.dayLabel,
            style = AzkryTextStyles.Caption,
            color = if (day.isToday) AzkryTheme.colors.AccentYellow else AzkryTheme.colors.TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        ProgressRing(
            progress = day.percent / 100f,
            size = 28.dp,
            strokeWidth = 3.dp,
            color = if (day.isToday) AzkryTheme.colors.AccentYellow else AzkryTheme.colors.AccentGreen,
        )
        Text(
            text = day.dayOfMonth.toString(),
            style = AzkryTextStyles.Callout,
            color = if (day.isToday) AzkryTheme.colors.AccentYellow else AzkryTheme.colors.TextPrimary,
        )
    }
}

@Composable
private fun SectionTitle(title: String, counter: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AzkrySpacing.Sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = AzkryTextStyles.Title2,
            color = AzkryTheme.colors.TextPrimary,
        )
        Text(
            text = counter,
            style = AzkryTextStyles.Callout,
            color = AzkryTheme.colors.TextSecondary,
        )
    }
}

@Composable
private fun PrayersRow(
    prayers: List<PrayerCell>,
    onPrayerToggled: (PrayerCell) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.Sm),
    ) {
        prayers.forEach { cell ->
            Surface(
                onClick = { onPrayerToggled(cell) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(AzkryRadius.Lg),
                color = AzkryTheme.colors.SurfaceCard,
                contentColor = AzkryTheme.colors.TextPrimary,
                border = BorderStroke(
                    1.dp,
                    if (cell.completed) AzkryTheme.colors.AccentGreen else AzkryTheme.colors.BorderDefault,
                ),
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = AzkrySpacing.Xs,
                        vertical = AzkrySpacing.S12,
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs),
                ) {
                    Text(
                        text = stringResource(cell.prayer.labelRes()),
                        style = AzkryTextStyles.Callout,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Icon(
                        imageVector = if (cell.completed) {
                            Icons.Outlined.CheckCircle
                        } else {
                            prayerIcon(cell.prayer)
                        },
                        contentDescription = null,
                        tint = if (cell.completed) {
                            AzkryTheme.colors.AccentGreen
                        } else {
                            AzkryTheme.colors.TextSecondary
                        },
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = cell.time,
                        style = AzkryTextStyles.Footnote,
                        color = AzkryTheme.colors.TextSecondary,
                    )
                }
            }
        }
    }
}

@Composable
private fun AdhkarProgressRow(row: AdhkarCategoryProgress) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Card),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(
            1.dp,
            if (row.isCompleted) AzkryTheme.colors.AccentGreen else AzkryTheme.colors.BorderDefault,
        ),
    ) {
        Row(
            modifier = Modifier.padding(AzkrySpacing.Md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = row.title,
                style = AzkryTextStyles.Headline,
                modifier = Modifier.weight(1f),
            )
            ProgressRing(
                progress = if (row.totalCount == 0) 0f else row.completedCount.toFloat() / row.totalCount,
                size = 44.dp,
                strokeWidth = 3.dp,
                color = if (row.isCompleted) AzkryTheme.colors.AccentGreen else AzkryTheme.colors.RingTrack,
                label = row.completedCount.toString(),
                labelStyle = AzkryTextStyles.Callout,
            )
        }
    }
}

@Composable
private fun TaskRow(
    title: String,
    subtitle: String,
    completed: Boolean,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(AzkryRadius.Card),
        color = AzkryTheme.colors.SurfaceCard,
        contentColor = AzkryTheme.colors.TextPrimary,
        border = BorderStroke(
            1.dp,
            if (completed) AzkryTheme.colors.AccentGreen else AzkryTheme.colors.BorderDefault,
        ),
    ) {
        Row(
            modifier = Modifier.padding(AzkrySpacing.Md),
            horizontalArrangement = Arrangement.spacedBy(AzkrySpacing.S12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AzkrySpacing.Xs),
            ) {
                Text(text = title, style = AzkryTextStyles.Headline)
                Text(
                    text = subtitle,
                    style = AzkryTextStyles.Subhead,
                    color = AzkryTheme.colors.TextSecondary,
                )
            }
            if (completed) {
                Surface(
                    modifier = Modifier.size(28.dp),
                    shape = CircleShape,
                    color = AzkryTheme.colors.AccentGreen,
                    contentColor = AzkryTheme.colors.Night900,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}

@AzkryPreview
@Composable
private fun TrackingContentPreview() {
    AzkryPreviewSurface(padding = 0.dp) {
        TrackingContent(
            state = TrackingUiState(
                hijriDate = "1448-02-03",
                gregorianDate = "2026-07-17",
                todayPercent = 10,
                weekDays = (11..17).map { day ->
                    WeekDayRing(
                        dayLabel = "جمعة",
                        dayOfMonth = day,
                        percent = (day - 11) * 12,
                        isToday = day == 17,
                    )
                },
                prayers = Prayer.obligatory.mapIndexed { index, prayer ->
                    PrayerCell(prayer = prayer, time = "4:15", completed = index == 0)
                },
                adhkarRows = listOf(
                    AdhkarCategoryProgress(
                        categoryId = 1,
                        categoryKey = "morning",
                        title = "أذكار الصباح",
                        completedCount = 0,
                        totalCount = 25,
                    ),
                ),
                completedTasks = setOf(WorshipTask.QuranDaily),
            ),
            onBack = {},
            onPrayerToggled = {},
            onTaskToggled = { _, _ -> },
        )
    }
}
