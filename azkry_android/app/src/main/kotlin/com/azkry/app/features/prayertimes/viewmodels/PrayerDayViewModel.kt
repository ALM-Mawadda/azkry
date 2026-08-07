package com.azkry.app.features.prayertimes.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azkry.app.app.AppSettingsService
import com.azkry.app.core.models.Prayer
import com.azkry.app.core.utilities.CurrentDateProvider
import com.azkry.app.core.utilities.toDateKey
import com.azkry.app.core.utilities.toHijriIso
import com.azkry.app.core.utilities.toHijriMonthName
import com.azkry.app.features.home.models.HomeDayView
import com.azkry.app.features.prayertimes.models.ForbiddenWindow
import com.azkry.app.features.prayertimes.models.PrayerDayView
import com.azkry.app.features.prayertimes.services.PrayerSettingsService
import com.azkry.app.features.prayertimes.services.PrayerTimesService
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@Immutable
data class PrayerTimeRow(
    val prayer: Prayer,
    /** Adhan time for the configured location, e.g. "4:19". */
    val time: String,
    /** Before noon — the view renders the ص/م marker from resources. */
    val isAm: Boolean,
    /**
     * Start of Ishraq, derived from the calculated sunrise. Null for every
     * other prayer: iqama is set by the mosque, so the app cannot state it.
     */
    val ishraqTime: String?,
    val isNext: Boolean,
)

@Immutable
data class PrayerDayUiState(
    val dateKey: String,
    val hijriIso: String,
    val hijriMonthName: String,
    val dayName: String,
    /** The configured location — detected or chosen — these times belong to. */
    val cityName: String,
    val isToday: Boolean,
    /** Null on days that are not today — there is no live countdown then. */
    val nextPrayer: Prayer?,
    val nextPrayerTime: String,
    val nextPrayerIsAm: Boolean,
    val countdown: String,
    val forbiddenWindow: ForbiddenWindow?,
    val rows: List<PrayerTimeRow>,
)

private val timeFormatter = DateTimeFormatter.ofPattern("h:mm", Locale.ENGLISH)
private val dayNameFormatter = DateTimeFormatter.ofPattern("EEEE", Locale.forLanguageTag("ar"))

@HiltViewModel
class PrayerDayViewModel @Inject constructor(
    private val prayerTimesService: PrayerTimesService,
    prayerSettingsService: PrayerSettingsService,
    appSettingsService: AppSettingsService,
    currentDateProvider: CurrentDateProvider,
    private val clock: Clock,
) : ViewModel() {
    private val dayOffset = MutableStateFlow(0L)

    private val hijriOffsetDays = appSettingsService.settings.map { it.hijriOffsetDays }

    // Seconds resolution: the next-prayer countdown ticks like the reference.
    private val ticker = flow {
        while (true) {
            emit(clock.instant())
            delay(1_000)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private val currentDate = prayerSettingsService.settings.flatMapLatest { settings ->
        currentDateProvider.observeCurrentDate(settings.zoneId)
    }

    private val selectedDate = combine(dayOffset, currentDate) { offset, today ->
        today.plusDays(offset)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<PrayerDayUiState?> =
        selectedDate
            .flatMapLatest { date ->
                combine(
                    prayerTimesService.observeTimes(date),
                    hijriOffsetDays,
                    currentDate,
                    ticker,
                ) { dayTimes, hijriOffset, today, now ->
                    val isToday = date == today
                    val zoneId = dayTimes.settings.zoneId
                    val next = if (isToday) {
                        prayerTimesService.nextPrayer(now, dayTimes)
                    } else {
                        null
                    }
                    // Past Isha the next prayer is tomorrow's Fajr, so the
                    // countdown keeps running — but no row on *this* day is it.
                    val nextOnThisDay = next?.takeIf { it.at.toLocalDate() == date }
                    val times = dayTimes.times
                    val hijriDate = date.plusDays(hijriOffset.toLong())

                    PrayerDayUiState(
                        dateKey = date.toDateKey(),
                        hijriIso = hijriDate.toHijriIso(),
                        hijriMonthName = hijriDate.toHijriMonthName(),
                        dayName = date.format(dayNameFormatter),
                        cityName = dayTimes.cityName,
                        isToday = isToday,
                        nextPrayer = next?.prayer,
                        nextPrayerTime = next?.at?.toLocalTime()?.format(timeFormatter).orEmpty(),
                        nextPrayerIsAm = next?.at?.toLocalTime()?.isAm() ?: true,
                        countdown = next?.remaining?.let(HomeDayView::countdownClock).orEmpty(),
                        forbiddenWindow = if (isToday) {
                            PrayerDayView.forbiddenWindow(
                                now.atZone(zoneId).toLocalTime(),
                                times.times,
                            )
                        } else {
                            null
                        },
                        rows = Prayer.entries.map { prayer ->
                            val adhan = times[prayer]
                            PrayerTimeRow(
                                prayer = prayer,
                                time = adhan.format(timeFormatter),
                                isAm = adhan.isAm(),
                                ishraqTime = if (prayer == Prayer.Sunrise) {
                                    PrayerDayView.ishraqTime(adhan).format(timeFormatter)
                                } else {
                                    null
                                },
                                isNext = nextOnThisDay?.prayer == prayer,
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
        dayOffset.update { it - 1 }
    }

    fun onNextDay() {
        dayOffset.update { it + 1 }
    }

    fun onBackToToday() {
        dayOffset.value = 0L
    }
}

private fun LocalTime.isAm(): Boolean = hour < 12
