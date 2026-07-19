import SwiftUI

enum HomeTab: String, CaseIterable, Identifiable {
    case misc
    case prayer
    case qibla
    case favorites
    case counter

    var id: String { rawValue }

    var title: String {
        switch self {
        case .misc: "المنوعات"
        case .prayer: "الصلاة"
        case .qibla: "القبلة"
        case .favorites: "المفضلة"
        case .counter: "العداد"
        }
    }
}

struct HomeNavigation {
    let onOpenTracking: () -> Void
    let onOpenAdhkar: () -> Void
    let onOpenMushaf: () -> Void
    let onOpenPages: () -> Void
    let onOpenSettings: () -> Void
    let onOpenSearch: () -> Void
    let onOpenPrayerTimes: () -> Void
    let onOpenDhikrCategory: (String) -> Void
    let onOpenFriday: () -> Void
    let onOpenHijriCalendar: () -> Void
    let onOpenExclusive: () -> Void
}

struct HomeView: View {
    let navigation: HomeNavigation

    @State private var viewModel = HomeViewModel()
    @State private var selectedTab: HomeTab = .misc
    @State private var scrollOffset: CGFloat = 0
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    private var headerHeight: CGFloat {
        dynamicTypeSize.isAccessibilitySize
            ? AzkryTheme.Layout.accessibilityHomeHeaderMinimumHeight
            : AzkryTheme.Layout.homeHeaderHeight
    }

    private var contentOpacity: Double {
        let fadeDistance = headerHeight * 0.58
        return max(0, min(1, 1 - Double(scrollOffset / fadeDistance)))
    }

    private var headerActionsOpacity: Double {
        max(0, min(1, 1 - Double(scrollOffset / 90)))
    }

    private var isCollapsed: Bool {
        compactProgress >= 0.98
    }

    private var compactProgress: Double {
        let start = headerHeight - 190
        let distance: CGFloat = 90
        return max(0, min(1, Double((scrollOffset - start) / distance)))
    }

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 0, pinnedViews: [.sectionHeaders]) {
                HomeSkyHeader(
                    state: viewModel.state,
                    contentOpacity: contentOpacity,
                    actionsOpacity: headerActionsOpacity,
                    onOpenSettings: navigation.onOpenSettings,
                    onOpenSearch: navigation.onOpenSearch,
                    onResumeReading: navigation.onOpenMushaf
                )

                Section {
                    Group {
                        if selectedTab == .misc {
                            HomeMiscContent(
                                state: viewModel.state,
                                navigation: navigation
                            )
                        } else {
                            selectedTabContent
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .background(AzkryColors.background)
                } header: {
                    HomePinnedBar(
                        selectedTab: $selectedTab,
                        compactProgress: compactProgress,
                        onOpenPrayerTimes: navigation.onOpenPrayerTimes
                    )
                }
            }
        }
        .background(AzkryColors.background)
        .overlay(alignment: .top) {
            if isCollapsed {
                CompactHomeHeaderRow(
                    onOpenSettings: navigation.onOpenSettings,
                    onOpenSearch: navigation.onOpenSearch
                )
                .background(.ultraThinMaterial)
            }
        }
        .onScrollGeometryChange(for: CGFloat.self) { geometry in
            max(0, geometry.contentOffset.y + geometry.contentInsets.top)
        } action: { _, newValue in
            scrollOffset = newValue
        }
        .task { await viewModel.run() }
        .toolbar(.hidden, for: .navigationBar)
    }

    @ViewBuilder
    private var selectedTabContent: some View {
        switch selectedTab {
        case .misc:
            EmptyView()
        case .prayer:
            EmptyView()
        case .qibla:
            QiblaView(embedded: true)
        case .favorites:
            FavoritesView(embedded: true)
        case .counter:
            TasbihCounterView(embedded: true)
        }
    }
}

private struct HomePinnedBar: View {
    @Binding var selectedTab: HomeTab
    let compactProgress: Double
    let onOpenPrayerTimes: () -> Void

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    var body: some View {
        VStack(spacing: 0) {
            Color.clear
                .frame(height: 62 * compactProgress)
                .accessibilityHidden(true)

            if dynamicTypeSize.isAccessibilitySize {
                ScrollView(.horizontal) {
                    HStack(spacing: 4) {
                        ForEach(HomeTab.allCases) { tab in
                            tabButton(tab)
                                .frame(minWidth: 118)
                        }
                    }
                    .padding(.horizontal, 8)
                }
                .scrollIndicators(.hidden)
            } else {
                HStack(spacing: 0) {
                    ForEach(HomeTab.allCases) { tab in
                        tabButton(tab)
                            .frame(maxWidth: .infinity)
                    }
                }
            }
        }
        .background(.ultraThinMaterial)
    }

    private func tabButton(_ tab: HomeTab) -> some View {
        Button(tab.title) {
            if tab == .prayer {
                onOpenPrayerTimes()
            } else {
                selectedTab = tab
            }
        }
        .font(AzkryTypography.callout)
        .foregroundStyle(
            selectedTab == tab
                ? AzkryColors.textPrimary
                : AzkryColors.textSecondary
        )
        .frame(minHeight: 48)
        .contentShape(Rectangle())
        .overlay(alignment: .bottom) {
            if selectedTab == tab {
                Capsule()
                    .fill(AzkryColors.indigo)
                    .frame(width: 24, height: 3)
            }
        }
        .accessibilityAddTraits(selectedTab == tab ? .isSelected : [])
        .accessibilityHint(tab == .prayer ? "يفتح مواقيت الصلاة" : "")
    }
}

private struct CompactHomeHeaderRow: View {
    let onOpenSettings: () -> Void
    let onOpenSearch: () -> Void

    var body: some View {
        HStack {
            Text("أذكاري")
                .font(AzkryTypography.wordmark)
                .foregroundStyle(AzkryColors.textPrimary)

            Spacer()

            Button("البحث", systemImage: "magnifyingglass", action: onOpenSearch)
                .labelStyle(.iconOnly)
                .frame(
                    width: AzkryTheme.Layout.minimumTapTarget,
                    height: AzkryTheme.Layout.minimumTapTarget
                )
            Button("الإعدادات", systemImage: "gearshape", action: onOpenSettings)
                .labelStyle(.iconOnly)
                .frame(
                    width: AzkryTheme.Layout.minimumTapTarget,
                    height: AzkryTheme.Layout.minimumTapTarget
                )
        }
        .font(.system(size: 17, weight: .medium))
        .foregroundStyle(AzkryColors.textPrimary)
        .padding(.horizontal, 18)
        .padding(.top, 10)
        .padding(.bottom, 8)
        .frame(height: 62)
        .dynamicTypeSize(.xSmall ... .xxxLarge)
    }
}

private struct HomeMiscContent: View {
    let state: HomeViewState
    let navigation: HomeNavigation

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        VStack(spacing: 12) {
            if let errorMessage = state.errorMessage {
                Label(errorMessage, systemImage: "exclamationmark.triangle.fill")
                    .font(AzkryTypography.callout)
                    .foregroundStyle(AzkryColors.danger)
                    .frame(maxWidth: .infinity, alignment: .trailing)
                    .padding(12)
                    .background(AzkryColors.danger.opacity(0.12), in: .rect(cornerRadius: 16))
                    .accessibilityLabel("خطأ: \(errorMessage)")
            }

            if dynamicTypeSize.isAccessibilitySize {
                VStack(alignment: .trailing, spacing: 10) {
                    countdownPhrase
                    Text(state.countdownClock)
                        .font(AzkryTypography.headline)
                        .foregroundStyle(AzkryColors.textSecondary)
                        .environment(\.layoutDirection, .leftToRight)
                }
                .frame(maxWidth: .infinity, alignment: .trailing)
                .padding(.vertical, 8)
            } else {
                HStack(spacing: 9) {
                    countdownPhrase

                    Spacer(minLength: 8)

                    Text(state.countdownClock)
                        .font(AzkryTypography.headline)
                        .foregroundStyle(AzkryColors.textSecondary)
                        .environment(\.layoutDirection, .leftToRight)
                }
                .padding(.vertical, 8)
            }

            HomeSectionCard(
                title: "متابعتي",
                subtitle: "تابع عباداتك اليومية",
                systemImage: "chart.bar.fill",
                action: navigation.onOpenTracking
            )
            HomeSectionCard(
                title: "المصحف",
                subtitle: "تابع القراءة والختمة",
                systemImage: "book.closed.fill",
                action: navigation.onOpenMushaf,
                secondary: HomeSectionLinkAction(
                    title: "متابعة القراءة",
                    subtitle: "من آخر سورة فتحتها",
                    systemImage: "bookmark.fill",
                    action: navigation.onOpenMushaf
                )
            )
            HomeSectionCard(
                title: "الأذكار والأدعية",
                subtitle: "أدعية وأذكار اليوم والليلة",
                systemImage: "sparkles",
                action: navigation.onOpenAdhkar,
                secondary: HomeSectionLinkAction(
                    title: state.suggestedAdhkarTitle,
                    subtitle: "الورد المناسب لهذا الوقت",
                    systemImage: suggestedAdhkarIcon,
                    action: { navigation.onOpenDhikrCategory(suggestedAdhkarKey) }
                )
            )
            HomeSectionCard(
                title: "صفحات",
                subtitle: "أسماء الله والسنن والأدعية",
                systemImage: "square.grid.2x2.fill",
                action: navigation.onOpenPages,
                secondary: HomeSectionLinkAction(
                    title: state.isFriday ? "يوم الجمعة" : "التقويم الهجري",
                    subtitle: state.isFriday ? "سنن الجمعة وسورة الكهف" : "الشهر الهجري الحالي",
                    systemImage: state.isFriday ? "sun.max.fill" : "calendar",
                    action: state.isFriday
                        ? navigation.onOpenFriday
                        : navigation.onOpenHijriCalendar
                )
            )
            HomeSectionCard(
                title: "حصريات",
                subtitle: "أذكار الحج والعمرة والأحبة والصغار",
                systemImage: "seal.fill",
                action: navigation.onOpenExclusive
            )

            Text("استكشف")
                .font(AzkryTypography.title2)
                .frame(maxWidth: .infinity, alignment: .trailing)
                .padding(.top, 10)

            HomeSectionCard(
                title: "مواقيت الصلاة",
                subtitle: "مواقيت اليوم حسب موقعك",
                systemImage: "clock.fill",
                action: navigation.onOpenPrayerTimes
            )
            HomeSectionCard(
                title: "التقويم الهجري",
                subtitle: "تقويم أم القرى",
                systemImage: "calendar",
                action: navigation.onOpenHijriCalendar
            )
            HomeSectionCard(
                title: "يوم الجمعة",
                subtitle: "السنن وسورة الكهف",
                systemImage: "sun.max.fill",
                action: navigation.onOpenFriday
            )
        }
        .padding(.horizontal, AzkryTheme.Layout.pageHorizontalPadding)
        .padding(.vertical, 12)
    }

    private var suggestedAdhkarKey: String {
        switch state.phase {
        case .dawn, .day: "morning"
        case .afternoon, .dusk: "evening"
        case .night: "sleep"
        }
    }

    private var suggestedAdhkarIcon: String {
        switch state.phase {
        case .dawn, .day: "sunrise.fill"
        case .afternoon, .dusk: "sunset.fill"
        case .night: "moon.stars.fill"
        }
    }

    private var countdownPhrase: some View {
        HStack(alignment: .firstTextBaseline, spacing: 9) {
                Capsule()
                    .fill(AzkryColors.yellow)
                    .frame(width: 4, height: 22)

                Text("أذان \(state.upcomingPrayer.title) بعد \(state.countdownPhrase)")
                    .font(AzkryTypography.headline)
                    .foregroundStyle(AzkryColors.textPrimary)
        }
        .accessibilityElement(children: .combine)
    }
}
