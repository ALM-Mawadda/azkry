package com.azkry.app.features.home.viewmodels

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azkry.app.app.AppSettingsService
import com.azkry.app.core.models.DhikrCategory
import com.azkry.app.core.models.Prayer
import com.azkry.app.core.utilities.toDateKey
import com.azkry.app.core.utilities.toHijriDay
import com.azkry.app.core.utilities.toHijriMonthName
import com.azkry.app.features.adhkar.services.AdhkarService
import com.azkry.app.features.home.models.HeaderPhase
import com.azkry.app.features.home.models.HomeDayView
import com.azkry.app.features.mushaf.services.QuranService
import com.azkry.app.features.prayertimes.services.PrayerTimesService
import com.azkry.app.features.tracking.services.WorshipTrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@Immutable
data class HomeUiState(
    val verse: String,
    val hijriDay: Int,
    val hijriDayMonth: String,
    val cityName: String,
    val headerPhase: HeaderPhase,
    /** The event that just passed (reading-start side of the strip). */
    val previousPrayer: Prayer,
    val previousTime: String,
    /** The upcoming event (opposite side of the strip). */
    val upcomingPrayer: Prayer,
    val upcomingTime: String,
    /** Live H:MM:SS until the next obligatory adhan. */
    val countdown: String,
    val countdownPrayer: Prayer,
    val trackingPercent: Int,
    val isFriday: Boolean,
    val suggestedCategory: DhikrCategory?,
    val quranBookmarkCount: Int,
)

/** Short rotating texts for the sky header; swapped hourly. */
private val HEADER_VERSES = listOf(
    "رَبَّنَا لَا تُؤَاخِذْنَا إِن نَّسِينَا أَوْ أَخْطَأْنَا",
    "أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ",
    "إِنَّ مَعَ الْعُسْرِ يُسْرًا",
    "فَاذْكُرُونِي أَذْكُرْكُمْ وَاشْكُرُوا لِي وَلَا تَكْفُرُونِ",
    "وَقُل رَّبِّ زِدْنِي عِلْمًا",
    "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي",
)

private val timeFormatter = DateTimeFormatter.ofPattern("hh:mm", Locale.ENGLISH)

@HiltViewModel
class HomeViewModel @Inject constructor(
    prayerTimesService: PrayerTimesService,
    trackingService: WorshipTrackingService,
    appSettingsService: AppSettingsService,
    adhkarService: AdhkarService,
    quranService: QuranService,
) : ViewModel() {
    private val hijriOffsetDays = appSettingsService.settings
        .map { it.hijriOffsetDays }
        .distinctUntilChanged()

    // Seconds resolution: the countdown ticks like the reference app.
    private val ticker = flow {
        while (true) {
            emit(LocalDateTime.now())
            delay(1_000)
        }
    }

    private val dateFlow = ticker.map(LocalDateTime::toLocalDate).distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val suggestedCategory = ticker
        .map { now -> HomeDayView.suggestedAdhkarKey(phaseAt(now)) }
        .distinctUntilChanged()
        .map { key -> adhkarService.categoryByKey(key) }

    private var latestTimes: Map<Prayer, java.time.LocalTime> = emptyMap()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<HomeUiState?> =
        combine(
            ticker,
            dateFlow.flatMapLatest { date -> prayerTimesService.observeTimes(date) },
            dateFlow.flatMapLatest { date -> trackingService.observeDay(date.toDateKey()) },
            combine(hijriOffsetDays, suggestedCategory, quranService.bookmarks) { offset, category, bookmarks ->
                Triple(offset, category, bookmarks.size)
            },
        ) { now, dayTimes, dayTracking, (hijriOffset, category, bookmarkCount) ->
            latestTimes = dayTimes.times.times
            val next = prayerTimesService.nextPrayer(now, dayTimes)
            val (previous, upcoming) = HomeDayView.stripEvents(now.toLocalTime(), latestTimes)
            val hijriDate = LocalDate.now().plusDays(hijriOffset.toLong())
            HomeUiState(
                verse = HEADER_VERSES[now.hour % HEADER_VERSES.size],
                hijriDay = hijriDate.toHijriDay(),
                hijriDayMonth = hijriDate.toHijriMonthName(),
                cityName = dayTimes.cityName,
                headerPhase = HomeDayView.phaseFor(now.toLocalTime(), latestTimes),
                previousPrayer = previous,
                previousTime = latestTimes.getValue(previous).format(timeFormatter),
                upcomingPrayer = upcoming,
                upcomingTime = latestTimes.getValue(upcoming).format(timeFormatter),
                countdown = formatCountdown(next.remaining),
                countdownPrayer = next.prayer,
                trackingPercent = dayTracking.percent,
                isFriday = LocalDate.now().dayOfWeek == DayOfWeek.FRIDAY,
                suggestedCategory = category,
                quranBookmarkCount = bookmarkCount,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    private fun phaseAt(now: LocalDateTime): HeaderPhase =
        HomeDayView.phaseFor(now.toLocalTime(), latestTimes)

    private fun formatCountdown(remaining: Duration): String {
        val totalSeconds = remaining.seconds.coerceAtLeast(0)
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.ENGLISH, "%d:%02d:%02d", hours, minutes, seconds)
    }
}
