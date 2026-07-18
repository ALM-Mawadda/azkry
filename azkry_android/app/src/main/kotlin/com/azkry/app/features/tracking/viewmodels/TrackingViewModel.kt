package com.azkry.app.features.tracking.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azkry.app.app.AppSettingsService
import com.azkry.app.core.models.Prayer
import com.azkry.app.core.utilities.toDateKey
import com.azkry.app.core.utilities.toHijriIso
import com.azkry.app.features.prayertimes.services.PrayerTimesService
import com.azkry.app.features.tracking.models.WorshipTask
import com.azkry.app.features.tracking.services.AdhkarCategoryProgress
import com.azkry.app.features.tracking.services.WorshipTrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class WeekDayRing(
    val dayLabel: String,
    val dayOfMonth: Int,
    val percent: Int,
    val isToday: Boolean,
)

@Immutable
data class PrayerCell(
    val prayer: Prayer,
    val time: String,
    val completed: Boolean,
)

@Immutable
data class TrackingUiState(
    val hijriDate: String,
    val gregorianDate: String,
    val todayPercent: Int,
    val weekDays: List<WeekDayRing>,
    val prayers: List<PrayerCell>,
    val adhkarRows: List<AdhkarCategoryProgress>,
    val completedTasks: Set<WorshipTask>,
)

private val dayLabelFormatter = DateTimeFormatter.ofPattern("EEE", Locale.forLanguageTag("ar"))
private val timeFormatter = DateTimeFormatter.ofPattern("h:mm", Locale.ENGLISH)

@HiltViewModel
class TrackingViewModel @Inject constructor(
    private val trackingService: WorshipTrackingService,
    prayerTimesService: PrayerTimesService,
    appSettingsService: AppSettingsService,
) : ViewModel() {
    private val today: LocalDate = LocalDate.now()

    // The design's week strip runs Saturday through Friday.
    private val weekDates: List<LocalDate> = run {
        val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.SATURDAY))
        (0L..6L).map(weekStart::plusDays)
    }

    val state: StateFlow<TrackingUiState?> =
        combine(
            trackingService.observeDay(today.toDateKey()),
            trackingService.observeWeekPercents(weekDates.map { it.toDateKey() }),
            prayerTimesService.observeTimes(today),
            appSettingsService.settings,
        ) { day, weekPercents, dayTimes, appSettings ->
            TrackingUiState(
                hijriDate = today.plusDays(appSettings.hijriOffsetDays.toLong()).toHijriIso(),
                gregorianDate = today.toDateKey(),
                todayPercent = day.percent,
                weekDays = weekDates.map { date ->
                    WeekDayRing(
                        dayLabel = date.format(dayLabelFormatter),
                        dayOfMonth = date.dayOfMonth,
                        percent = weekPercents[date.toDateKey()] ?: 0,
                        isToday = date == today,
                    )
                },
                prayers = Prayer.obligatory.map { prayer ->
                    PrayerCell(
                        prayer = prayer,
                        time = dayTimes.times[prayer].format(timeFormatter),
                        completed = prayer in day.completedPrayers,
                    )
                },
                adhkarRows = day.adhkarProgress,
                completedTasks = day.completedTasks,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    fun onPrayerToggled(cell: PrayerCell) {
        viewModelScope.launch {
            trackingService.setPrayerCompleted(today.toDateKey(), cell.prayer, !cell.completed)
        }
    }

    fun onTaskToggled(task: WorshipTask, currentlyCompleted: Boolean) {
        viewModelScope.launch {
            trackingService.setTaskCompleted(today.toDateKey(), task, !currentlyCompleted)
        }
    }
}
