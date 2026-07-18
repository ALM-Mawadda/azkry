package com.azkry.app.features.main

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.azkry.app.features.adhkar.views.AdhkarCategoriesView
import com.azkry.app.features.adhkar.views.DhikrReaderView
import com.azkry.app.features.adhkar.views.ExclusiveView
import com.azkry.app.features.calendar.views.HijriCalendarView
import com.azkry.app.features.friday.views.FridayView
import com.azkry.app.features.home.views.HomeNavigation
import com.azkry.app.features.home.views.HomeView
import com.azkry.app.features.mushaf.views.MushafView
import com.azkry.app.features.mushaf.views.SurahOpenRequest
import com.azkry.app.features.mushaf.views.SurahReaderView
import com.azkry.app.features.notifications.models.NotificationDestination
import com.azkry.app.features.notifications.views.NotificationSettingsView
import com.azkry.app.features.pages.models.PageKey
import com.azkry.app.features.pages.views.PageDetailView
import com.azkry.app.features.pages.views.PagesView
import com.azkry.app.features.prayertimes.views.PrayerTimesSettingsView
import com.azkry.app.features.search.views.SearchView
import com.azkry.app.features.settings.views.SettingsNavigation
import com.azkry.app.features.settings.views.SettingsView
import com.azkry.app.features.tracking.views.TrackingView

private const val KAHF_SURAH_NUMBER = 18

enum class MainScreen {
    Home,
    Tracking,
    AdhkarCategories,
    Mushaf,
    Pages,
    Friday,
    Search,
    Exclusive,
    Settings,
    PrayerTimesSettings,
    NotificationSettings,
    HijriCalendar,
}

/**
 * Root navigation for the single-role app: a state-driven shell (no nav
 * graph), matching the reference architecture. The dhikr reader and surah
 * reader layer on top so back returns to the launching screen.
 */
@Composable
fun MainShell(
    pendingDestination: NotificationDestination? = null,
    onDestinationConsumed: (NotificationDestination) -> Unit = {},
    shellViewModel: MainShellViewModel = hiltViewModel(),
) {
    var screen by rememberSaveable { mutableStateOf(MainScreen.Home) }
    var readerCategoryId by rememberSaveable { mutableStateOf<Long?>(null) }
    var readerSurahNumber by rememberSaveable { mutableStateOf<Int?>(null) }
    var readerSurahStartAyah by rememberSaveable { mutableStateOf<Int?>(null) }
    var readerSurahStartPage by rememberSaveable { mutableStateOf<Int?>(null) }
    var openPageKey by rememberSaveable { mutableStateOf<String?>(null) }
    // Friday is reachable from both home and the صفحات hub; back returns to
    // whichever launched it.
    var fridayFromPages by rememberSaveable { mutableStateOf(false) }

    fun openSurah(request: SurahOpenRequest) {
        readerSurahNumber = request.surahNumber
        readerSurahStartAyah = request.startAyah
        readerSurahStartPage = request.startPage
    }

    fun closeSurahReader() {
        readerSurahNumber = null
        readerSurahStartAyah = null
        readerSurahStartPage = null
    }

    // Notification taps land on the matching screen; adhkar reminders open
    // their category's reader directly.
    LaunchedEffect(pendingDestination) {
        val destination = pendingDestination ?: return@LaunchedEffect
        when (destination) {
            NotificationDestination.PrayerTimes -> {
                closeSurahReader()
                readerCategoryId = null
                screen = MainScreen.Home
            }

            NotificationDestination.MorningAdhkar,
            NotificationDestination.EveningAdhkar,
            -> {
                shellViewModel.categoryIdFor(destination)?.let { categoryId ->
                    closeSurahReader()
                    screen = MainScreen.AdhkarCategories
                    readerCategoryId = categoryId
                }
            }
        }
        onDestinationConsumed(destination)
    }

    BackHandler(
        enabled = screen != MainScreen.Home ||
            readerCategoryId != null ||
            readerSurahNumber != null ||
            openPageKey != null,
    ) {
        when {
            readerSurahNumber != null -> closeSurahReader()
            readerCategoryId != null -> readerCategoryId = null
            openPageKey != null -> openPageKey = null
            screen == MainScreen.PrayerTimesSettings -> screen = MainScreen.Settings
            screen == MainScreen.NotificationSettings -> screen = MainScreen.Settings
            screen == MainScreen.HijriCalendar -> screen = MainScreen.Settings
            screen == MainScreen.Friday && fridayFromPages -> {
                fridayFromPages = false
                screen = MainScreen.Pages
            }

            else -> screen = MainScreen.Home
        }
    }

    val openSurahNumber = readerSurahNumber
    if (openSurahNumber != null) {
        SurahReaderView(
            surahNumber = openSurahNumber,
            startAyah = readerSurahStartAyah,
            startPage = readerSurahStartPage,
            onBack = ::closeSurahReader,
        )
        return
    }

    val openReader = readerCategoryId
    if (openReader != null) {
        DhikrReaderView(
            categoryId = openReader,
            onBack = { readerCategoryId = null },
        )
        return
    }

    val openPage = openPageKey?.let { key -> PageKey.entries.firstOrNull { it.name == key } }
    if (openPage != null) {
        PageDetailView(
            pageKey = openPage,
            onBack = { openPageKey = null },
        )
        return
    }

    when (screen) {
        MainScreen.Home -> HomeView(
            navigation = HomeNavigation(
                onOpenTracking = { screen = MainScreen.Tracking },
                onOpenAdhkar = { screen = MainScreen.AdhkarCategories },
                onOpenMushaf = { screen = MainScreen.Mushaf },
                onOpenPages = { screen = MainScreen.Pages },
                onOpenFriday = {
                    fridayFromPages = false
                    screen = MainScreen.Friday
                },
                onOpenSettings = { screen = MainScreen.Settings },
                onOpenSearch = { screen = MainScreen.Search },
                onOpenExclusive = { screen = MainScreen.Exclusive },
                onOpenCategoryReader = { categoryId -> readerCategoryId = categoryId },
            ),
        )

        MainScreen.Tracking -> TrackingView(
            onBack = { screen = MainScreen.Home },
        )

        MainScreen.AdhkarCategories -> AdhkarCategoriesView(
            onBack = { screen = MainScreen.Home },
            onOpenCategory = { category -> readerCategoryId = category.id },
        )

        MainScreen.Mushaf -> MushafView(
            onBack = { screen = MainScreen.Home },
            onOpenSurah = ::openSurah,
        )

        MainScreen.Pages -> PagesView(
            onBack = { screen = MainScreen.Home },
            onOpenPage = { key -> openPageKey = key.name },
            onOpenFriday = {
                fridayFromPages = true
                screen = MainScreen.Friday
            },
        )

        MainScreen.Friday -> FridayView(
            onBack = {
                screen = if (fridayFromPages) MainScreen.Pages else MainScreen.Home
                fridayFromPages = false
            },
            onOpenKahf = { openSurah(SurahOpenRequest(KAHF_SURAH_NUMBER)) },
        )

        MainScreen.Search -> SearchView(
            onBack = { screen = MainScreen.Home },
            onOpenDhikrCategory = { categoryId -> readerCategoryId = categoryId },
            onOpenSurah = { surahNumber -> openSurah(SurahOpenRequest(surahNumber)) },
        )

        MainScreen.Exclusive -> ExclusiveView(
            onBack = { screen = MainScreen.Home },
            onOpenCategory = { category -> readerCategoryId = category.id },
            onOpenHijriCalendar = { screen = MainScreen.HijriCalendar },
        )

        MainScreen.Settings -> SettingsView(
            onBack = { screen = MainScreen.Home },
            navigation = SettingsNavigation(
                onOpenPrayerSettings = { screen = MainScreen.PrayerTimesSettings },
                onOpenNotificationSettings = { screen = MainScreen.NotificationSettings },
                onOpenHijriCalendar = { screen = MainScreen.HijriCalendar },
                onOpenExclusive = { screen = MainScreen.Exclusive },
            ),
        )

        MainScreen.PrayerTimesSettings -> PrayerTimesSettingsView(
            onBack = { screen = MainScreen.Settings },
        )

        MainScreen.NotificationSettings -> NotificationSettingsView(
            onBack = { screen = MainScreen.Settings },
        )

        MainScreen.HijriCalendar -> HijriCalendarView(
            onBack = { screen = MainScreen.Settings },
        )
    }
}
