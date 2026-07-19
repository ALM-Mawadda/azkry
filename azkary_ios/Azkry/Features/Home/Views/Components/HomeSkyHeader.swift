import SwiftUI

struct HomeSkyHeader: View {
    let state: HomeViewState
    let contentOpacity: Double
    let actionsOpacity: Double
    let onOpenSettings: () -> Void
    let onOpenSearch: () -> Void
    let onResumeReading: () -> Void

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        ZStack {
            SkyBackground(phase: state.phase)

            headerContent
            .opacity(contentOpacity)
        }
        .frame(
            minHeight: dynamicTypeSize.isAccessibilitySize
                ? AzkryTheme.Layout.accessibilityHomeHeaderMinimumHeight
                : nil
        )
        .frame(height: dynamicTypeSize.isAccessibilitySize ? nil : AzkryTheme.Layout.homeHeaderHeight)
        .accessibilityElement(children: .contain)
    }

    @ViewBuilder
    private var headerContent: some View {
        if dynamicTypeSize.isAccessibilitySize {
            VStack(spacing: 24) {
                HomeHeaderActions(
                    onOpenSettings: onOpenSettings,
                    onOpenSearch: onOpenSearch,
                    onResumeReading: onResumeReading
                )
                .opacity(actionsOpacity)

                titleBlock
                    .padding(.vertical, 8)

                PrayerStripView(state: state)
            }
            .padding(.top, 18)
            .padding(.horizontal, 18)
            .padding(.bottom, 24)
        } else {
            VStack(spacing: 0) {
                HomeHeaderActions(
                    onOpenSettings: onOpenSettings,
                    onOpenSearch: onOpenSearch,
                    onResumeReading: onResumeReading
                )
                .opacity(actionsOpacity)
                .padding(.top, 58)
                .padding(.horizontal, 18)

                Spacer(minLength: 28)
                titleBlock
                Spacer(minLength: 34)
                PrayerStripView(state: state)
            }
        }
    }

    private var titleBlock: some View {
        VStack(spacing: 9) {
            Text("أذكاري")
                .font(AzkryTypography.largeTitle)
            Label(state.city, systemImage: "location.fill")
                .font(AzkryTypography.callout)
                .foregroundStyle(AzkryColors.textOnSkySecondary)
        }
        .multilineTextAlignment(.center)
        .foregroundStyle(AzkryColors.textOnSky)
        .frame(maxWidth: .infinity)
    }
}

struct HomeHeaderActions: View {
    let onOpenSettings: () -> Void
    let onOpenSearch: () -> Void
    let onResumeReading: () -> Void

    var body: some View {
        HStack {
            HStack(spacing: 10) {
                HeaderCircleButton(
                    title: "الإعدادات",
                    systemImage: "gearshape",
                    action: onOpenSettings
                )
                HeaderCircleButton(
                    title: "البحث",
                    systemImage: "magnifyingglass",
                    action: onOpenSearch
                )
                HeaderCircleButton(
                    title: "العلامات",
                    systemImage: "bookmark",
                    action: onResumeReading
                )
            }

            Spacer()

            HeaderCircleButton(
                title: "متابعة القراءة",
                systemImage: "play.fill",
                action: onResumeReading
            )
        }
    }
}

private struct HeaderCircleButton: View {
    let title: String
    let systemImage: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: systemImage)
                .font(.system(size: 16, weight: .semibold))
                .foregroundStyle(AzkryColors.textOnSky)
                .frame(
                    width: AzkryTheme.Layout.minimumTapTarget,
                    height: AzkryTheme.Layout.minimumTapTarget
                )
                .background(Color.black.opacity(0.14), in: .circle)
                .overlay {
                    Circle().stroke(AzkryColors.indigo.opacity(0.85), lineWidth: 1)
                }
        }
        .buttonStyle(.plain)
        .accessibilityLabel(title)
    }
}
