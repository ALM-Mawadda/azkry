import SwiftData
import SwiftUI

struct RootView: View {
    @Environment(AppBootstrapper.self) private var bootstrapper
    @Environment(\.modelContext) private var modelContext
    @Environment(\.scenePhase) private var scenePhase
    private let reminderService: any PrayerReminderServiceProtocol = LocalPrayerReminderService()

    var body: some View {
        Group {
            switch bootstrapper.state {
            case .idle, .loading:
                ProgressView("جارٍ تجهيز أذكاري…")
                    .font(AzkryTypography.body)
                    .tint(AzkryColors.indigo)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(AzkryColors.background)

            case .ready:
                MainShellView()

            case .failed(let message):
                ContentUnavailableView {
                    Label("تعذر تجهيز التطبيق", systemImage: "exclamationmark.triangle")
                } description: {
                    Text(message)
                } actions: {
                    Button("إعادة المحاولة") {
                        Task { await bootstrapper.retry(in: modelContext) }
                    }
                    .buttonStyle(.borderedProminent)
                }
                .font(AzkryTypography.body)
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(AzkryColors.background)
            }
        }
        .task { await bootstrapper.start(in: modelContext) }
        .onChange(of: scenePhase) { _, phase in
            guard phase == .active else { return }
            Task { await reminderService.rescheduleIfEnabled() }
        }
    }
}
