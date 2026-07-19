import SwiftUI

struct PrayerStripView: View {
    let state: HomeViewState

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    private let chipWidth: CGFloat = 86
    private let chipTop: CGFloat = 2
    private let bandTop: CGFloat = 22

    var body: some View {
        if dynamicTypeSize.isAccessibilitySize {
            accessibilityStrip
        } else {
            standardStrip
        }
    }

    private var standardStrip: some View {
        ZStack(alignment: .top) {
            PrayerStripShape(
                chipWidth: chipWidth,
                chipTop: chipTop,
                bandTop: bandTop,
                cornerRadius: AzkryTheme.Radius.pill
            )
            .fill(AzkryColors.prayerStrip)

            HStack(spacing: 0) {
                PrayerStripTime(
                    prayer: state.previousPrayer,
                    time: state.previousTime
                )
                .frame(maxWidth: .infinity)

                Color.clear
                    .frame(width: chipWidth + 8)
                    .accessibilityHidden(true)

                PrayerStripTime(
                    prayer: state.upcomingPrayer,
                    time: state.upcomingTime
                )
                .frame(maxWidth: .infinity)
            }
            .padding(.horizontal, 12)
            .padding(.top, bandTop + 13)

            VStack(spacing: 3) {
                Text(state.hijriDay, format: .number.locale(Locale(identifier: "en_US_POSIX")))
                    .font(AzkryTypography.title2)
                Text(state.hijriMonth)
                    .font(AzkryTypography.caption)
                    .lineLimit(1)
                    .minimumScaleFactor(0.75)
            }
            .foregroundStyle(AzkryColors.textOnSky)
            .frame(width: chipWidth - 12)
            .padding(.top, chipTop + 12)
            .accessibilityElement(children: .combine)
            .accessibilityLabel("\(state.hijriDay) \(state.hijriMonth)")

            RoundedRectangle(cornerRadius: AzkryTheme.Radius.pill, style: .continuous)
                .stroke(AzkryColors.prayerStripOutline, lineWidth: 1)
                .frame(width: chipWidth, height: 82)
                .offset(y: chipTop)
                .allowsHitTesting(false)
                .accessibilityHidden(true)
        }
        .frame(height: 94)
        .clipped()
        .overlay(alignment: .bottom) {
            Rectangle()
                .fill(Color.white.opacity(0.18))
                .frame(height: 0.5)
        }
    }

    private var accessibilityStrip: some View {
        VStack(spacing: 16) {
            VStack(spacing: 3) {
                Text(state.hijriDay, format: .number.locale(Locale(identifier: "en_US_POSIX")))
                    .font(AzkryTypography.title2)
                Text(state.hijriMonth)
                    .font(AzkryTypography.caption)
            }
            .foregroundStyle(AzkryColors.textOnSky)
            .multilineTextAlignment(.center)
            .frame(maxWidth: .infinity)
            .accessibilityElement(children: .combine)
            .accessibilityLabel("\(state.hijriDay) \(state.hijriMonth)")

            Divider()
                .overlay(AzkryColors.prayerStripOutline)

            HStack(alignment: .top, spacing: 12) {
                PrayerStripTime(prayer: state.previousPrayer, time: state.previousTime)
                    .frame(maxWidth: .infinity)

                Rectangle()
                    .fill(AzkryColors.prayerStripOutline)
                    .frame(width: 1)
                    .accessibilityHidden(true)

                PrayerStripTime(prayer: state.upcomingPrayer, time: state.upcomingTime)
                    .frame(maxWidth: .infinity)
            }
        }
        .padding(18)
        .foregroundStyle(AzkryColors.textOnSky)
        .background(
            AzkryColors.prayerStrip,
            in: .rect(cornerRadius: AzkryTheme.Radius.card)
        )
        .overlay {
            RoundedRectangle(cornerRadius: AzkryTheme.Radius.card, style: .continuous)
                .stroke(AzkryColors.prayerStripOutline, lineWidth: 1)
        }
    }
}

private struct PrayerStripTime: View {
    let prayer: Prayer
    let time: String

    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        Group {
            if dynamicTypeSize.isAccessibilitySize {
                VStack(spacing: 6) {
                    Text(prayer.title)
                    Text(time)
                        .environment(\.layoutDirection, .leftToRight)
                }
            } else {
                ViewThatFits(in: .horizontal) {
                    HStack(spacing: 6) {
                        Text(prayer.title)
                        Text(time)
                            .environment(\.layoutDirection, .leftToRight)
                    }

                    VStack(spacing: 3) {
                        Text(prayer.title)
                        Text(time)
                            .environment(\.layoutDirection, .leftToRight)
                    }
                }
            }
        }
        .multilineTextAlignment(.center)
        .font(AzkryTypography.headline)
        .foregroundStyle(AzkryColors.textOnSky)
        .lineLimit(dynamicTypeSize.isAccessibilitySize ? 3 : 1)
        .minimumScaleFactor(dynamicTypeSize.isAccessibilitySize ? 1 : 0.7)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(prayer.title) \(time)")
    }
}

private struct PrayerStripShape: Shape {
    let chipWidth: CGFloat
    let chipTop: CGFloat
    let bandTop: CGFloat
    let cornerRadius: CGFloat

    func path(in rect: CGRect) -> Path {
        var path = Path()
        path.addRect(
            CGRect(
                x: rect.minX,
                y: rect.minY + bandTop,
                width: rect.width,
                height: rect.height - bandTop
            )
        )
        path.addRoundedRect(
            in: CGRect(
                x: rect.midX - chipWidth / 2,
                y: rect.minY + chipTop,
                width: chipWidth,
                height: rect.height + 10
            ),
            cornerSize: CGSize(width: cornerRadius, height: cornerRadius)
        )
        return path
    }
}
