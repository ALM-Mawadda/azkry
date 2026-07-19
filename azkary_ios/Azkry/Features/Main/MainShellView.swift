import SwiftUI

struct MainShellView: View {
    @Environment(AppState.self) private var appState

    var body: some View {
        @Bindable var appState = appState

        NavigationStack(path: $appState.path) {
            HomeView(
                navigation: HomeNavigation(
                    onOpenTracking: { appState.path.append(.tracking) },
                    onOpenAdhkar: { appState.path.append(.adhkar) },
                    onOpenMushaf: { appState.path.append(.mushaf) },
                    onOpenPages: { appState.path.append(.pages) },
                    onOpenSettings: { appState.path.append(.settings) },
                    onOpenSearch: { appState.path.append(.search) },
                    onOpenPrayerTimes: { appState.path.append(.prayerTimes) },
                    onOpenDhikrCategory: { appState.path.append(.dhikrCategory($0)) },
                    onOpenFriday: { appState.path.append(.friday) },
                    onOpenHijriCalendar: { appState.path.append(.hijriCalendar) },
                    onOpenExclusive: { appState.path.append(.exclusive) }
                )
            )
            .navigationDestination(for: MainDestination.self) { destination in
                destinationView(destination)
            }
        }
    }

    @ViewBuilder
    private func destinationView(_ destination: MainDestination) -> some View {
        switch destination {
        case .adhkar:
            AdhkarLibraryView()
        case let .dhikrCategory(categoryKey):
            DhikrReaderView(categoryKey: categoryKey)
        case .tracking:
            TrackingView()
        case .mushaf:
            MushafView()
        case let .surah(request):
            SurahReaderView(request: request)
        case .pages:
            PagesView()
        case let .page(key):
            PageDetailView(key: key)
        case .settings:
            SettingsView()
        case .search:
            AdhkarSearchView()
        case .prayerTimes:
            PrayerTimesView()
        case .hijriCalendar:
            HijriCalendarView()
        case .friday:
            FridayView()
        case .qibla:
            QiblaView()
        case .counter:
            TasbihCounterView()
        case .favorites:
            FavoritesView()
        case .exclusive:
            ExclusiveAdhkarView()
        }
    }
}
