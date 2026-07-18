package com.azkry.app.features.prayertimes.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azkry.app.app.AppSettingsService
import com.azkry.app.core.models.Prayer
import com.azkry.app.core.utilities.toDateKey
import com.azkry.app.core.utilities.toHijriDayMonth
import com.azkry.app.features.prayertimes.services.PrayerTimesService
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@Immutable
data class PrayerTimeRow(
    val prayer: Prayer,
    val time: String,
    val isNext: Boolean,
)

@Immutable
data class PrayerDayUiState(
    val dateKey: String,
    val hijriLabel: String,
    val cityName: String,
    val isToday: Boolean,
    val rows: List<PrayerTimeRow>,
)

private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm", Locale.ENGLISH)

@HiltViewModel
class PrayerDayViewModel @Inject constructor(
    private val prayerTimesService: PrayerTimesService,
    appSettingsService: AppSettingsService,
) : ViewModel() {
    private val selectedDate = MutableStateFlow(LocalDate.now())

    private val hijriOffsetDays = appSettingsService.settings.map { it.hijriOffsetDays }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<PrayerDayUiState?> =
        selectedDate
            .flatMapLatest { date ->
                combine(prayerTimesService.observeTimes(date), hijriOffsetDays) { dayTimes, hijriOffset ->
                    val today = LocalDate.now()
                    val next = if (date == today) {
                        prayerTimesService.nextPrayer(LocalDateTime.now(), dayTimes)
                            .takeIf { it.at.toLocalDate() == today }
                    } else {
                        null
                    }
                    PrayerDayUiState(
                        dateKey = date.toDateKey(),
                        hijriLabel = date.plusDays(hijriOffset.toLong()).toHijriDayMonth(),
                        cityName = dayTimes.cityName,
                        isToday = date == today,
                        rows = Prayer.entries.map { prayer ->
                            PrayerTimeRow(
                                prayer = prayer,
                                time = dayTimes.times[prayer].format(timeFormatter),
                                isNext = next?.prayer == prayer,
                            )
                        },
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = null,
            )

    fun onPreviousDay() {
        selectedDate.update { it.minusDays(1) }
    }

    fun onNextDay() {
        selectedDate.update { it.plusDays(1) }
    }

    fun onBackToToday() {
        selectedDate.update { LocalDate.now() }
    }
}
