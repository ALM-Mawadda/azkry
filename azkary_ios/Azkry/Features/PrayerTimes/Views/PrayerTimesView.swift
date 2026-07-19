import SwiftUI

struct PrayerTimesView: View {
    @State private var viewModel = PrayerTimesViewModel()

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                if let errorMessage = viewModel.errorMessage {
                    InlineErrorView(message: errorMessage)
                }

                if let schedule = viewModel.schedule {
                    VStack(spacing: 5) {
                        Text("مواقيت اليوم")
                            .font(AzkryTypography.title)
                        Label(schedule.city, systemImage: "location.fill")
                            .font(AzkryTypography.callout)
                            .foregroundStyle(AzkryColors.textSecondary)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 8)
                }

                VStack(spacing: 0) {
                    ForEach(Array(Prayer.allCases.enumerated()), id: \.element) { index, prayer in
                        HStack {
                            Text(viewModel.displayTime(for: prayer))
                                .font(AzkryTypography.title2)
                                .environment(\.layoutDirection, .leftToRight)
                            Spacer()
                            Text(prayer.title)
                                .font(AzkryTypography.headline)
                        }
                        .padding(.horizontal, 18)
                        .frame(minHeight: 62)
                        .accessibilityElement(children: .combine)
                        .accessibilityLabel("\(prayer.title) \(viewModel.displayTime(for: prayer))")

                        if index < Prayer.allCases.count - 1 {
                            Divider().padding(.horizontal, 18)
                        }
                    }
                }
                .background(AzkryColors.surface, in: .rect(cornerRadius: AzkryTheme.Radius.card))
                .overlay {
                    RoundedRectangle(cornerRadius: AzkryTheme.Radius.card)
                        .stroke(AzkryColors.border, lineWidth: 0.75)
                }
            }
            .padding(AzkryTheme.Layout.pageHorizontalPadding)
        }
        .background(AzkryColors.background)
        .navigationTitle("مواقيت الصلاة")
        .task { viewModel.load() }
    }
}
