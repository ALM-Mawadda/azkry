import SwiftData
import SwiftUI

@main
struct AzkryApp: App {
    @State private var appState = AppState()
    @State private var bootstrapper = AppBootstrapper()
    @State private var persistence = PersistenceController()
    @AppStorage(StorageKeys.appearanceMode)
    private var appearanceMode: Int = AppearanceMode.dark.rawValue

    var body: some Scene {
        WindowGroup {
            AppContainerView(persistence: persistence)
                .environment(appState)
                .environment(bootstrapper)
                .environment(\.locale, Locale(identifier: "ar"))
                .environment(\.layoutDirection, .rightToLeft)
                .preferredColorScheme(
                    AppearanceMode(rawValue: appearanceMode)?.colorScheme
                )
                .tint(AzkryColors.indigo)
        }
    }
}

private struct AppContainerView: View {
    let persistence: PersistenceController

    var body: some View {
        if let modelContainer = persistence.modelContainer {
            RootView()
                .modelContainer(modelContainer)
        } else {
            ContentUnavailableView {
                Label("تعذر فتح البيانات المحلية", systemImage: "externaldrive.badge.exclamationmark")
            } description: {
                Text(persistence.errorMessage ?? "حدث خطأ غير متوقع في قاعدة البيانات.")
            } actions: {
                Button("إعادة المحاولة", action: persistence.retry)
                    .buttonStyle(.borderedProminent)
            }
            .font(AzkryTypography.body)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .background(AzkryColors.background)
        }
    }
}
