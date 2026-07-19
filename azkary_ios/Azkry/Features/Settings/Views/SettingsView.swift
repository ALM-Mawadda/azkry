import SwiftUI
import SwiftData
import UniformTypeIdentifiers

struct SettingsView: View {
    @AppStorage(StorageKeys.appearanceMode)
    private var appearanceMode = AppearanceMode.dark.rawValue
    @State private var viewModel = SettingsViewModel()
    @Environment(\.modelContext) private var modelContext
    @State private var isExportingBackup = false
    @State private var isImportingBackup = false

    var body: some View {
        Form {
            Section("المظهر") {
                Picker("النمط", selection: $appearanceMode) {
                    ForEach(AppearanceMode.allCases) { mode in
                        Text(mode.title).tag(mode.rawValue)
                    }
                }
                .pickerStyle(.segmented)
            }

            Section("الموقع ومواقيت الصلاة") {
                LabeledContent("الموقع الحالي", value: viewModel.configuration.city)
                Picker("اختر مدينة", selection: Binding<PrayerLocationPreset?>(
                    get: { matchingPreset },
                    set: { preset in
                        guard let preset else { return }
                        Task { await viewModel.apply(preset) }
                    }
                )) {
                    Text("اختيار سريع").tag(nil as PrayerLocationPreset?)
                    ForEach(PrayerLocationPreset.allCases) { preset in
                        Text(preset.title).tag(Optional(preset))
                    }
                }
                Button {
                    Task { await viewModel.useCurrentLocation() }
                } label: {
                    Label(
                        viewModel.isLocating ? "جارٍ تحديد الموقع…" : "استخدام موقعي الحالي",
                        systemImage: "location.fill"
                    )
                }
                .disabled(viewModel.isLocating)

                if let locationError = viewModel.locationError {
                    InlineErrorView(message: locationError)
                }
                NavigationLink(value: MainDestination.prayerTimes) {
                    Label("عرض مواقيت الصلاة", systemImage: "clock")
                }
                NavigationLink(value: MainDestination.qibla) {
                    Label("اتجاه القبلة", systemImage: "location.north.circle")
                }
            }

            Section("التنبيهات") {
                Toggle("تنبيه الصلاة القادمة", isOn: Binding(
                    get: { viewModel.remindersEnabled },
                    set: { enabled in
                        Task { await viewModel.setRemindersEnabled(enabled) }
                    }
                ))
                .disabled(viewModel.isUpdatingReminders)

                Text("يُجدول أذكاري التنبيه القادم محليًا ولا يرسل بياناتك إلى أي خادم.")
                    .font(AzkryTypography.caption)
                    .foregroundStyle(AzkryColors.textSecondary)

                if let reminderError = viewModel.reminderError {
                    InlineErrorView(message: reminderError)
                }
            }

            Section("التقويم والمحتوى") {
                NavigationLink(value: MainDestination.hijriCalendar) {
                    Label("التقويم الهجري", systemImage: "calendar")
                }
                NavigationLink(value: MainDestination.favorites) {
                    Label("الأذكار المفضلة", systemImage: "heart")
                }
            }

            Section("النسخ الاحتياطي") {
                Button {
                    if viewModel.prepareBackup(in: modelContext) {
                        isExportingBackup = true
                    }
                } label: {
                    Label("تصدير نسخة احتياطية", systemImage: "square.and.arrow.up")
                }

                Button {
                    isImportingBackup = true
                } label: {
                    Label("استعادة نسخة احتياطية", systemImage: "square.and.arrow.down")
                }

                Text("تشمل النسخة التقدم والمفضلة والمتابعة والإعدادات، وتبقى على جهازك أو في المكان الذي تختاره.")
                    .font(AzkryTypography.caption)
                    .foregroundStyle(AzkryColors.textSecondary)

                if let backupError = viewModel.backupError {
                    InlineErrorView(message: backupError)
                }
            }

            if let savedMessage = viewModel.savedMessage {
                Section {
                    Label(savedMessage, systemImage: "checkmark.circle.fill")
                        .foregroundStyle(AzkryColors.green)
                }
            }

            Section("عن أذكاري") {
                LabeledContent("البيانات", value: "محلية على الجهاز")
                LabeledContent("الإنترنت", value: "غير مطلوب")
                Text("تحتاج النصوص الدينية إلى مراجعة علمية متخصصة قبل النشر العام.")
                    .font(AzkryTypography.caption)
                    .foregroundStyle(AzkryColors.textSecondary)
            }
        }
        .scrollContentBackground(.hidden)
        .background(AzkryColors.background)
        .navigationTitle("الإعدادات")
        .task { await viewModel.load() }
        .fileExporter(
            isPresented: $isExportingBackup,
            document: viewModel.backupDocument,
            contentType: .json,
            defaultFilename: "Azkry-Backup"
        ) { result in
            viewModel.backupExportFinished(result)
        }
        .fileImporter(
            isPresented: $isImportingBackup,
            allowedContentTypes: [.json],
            allowsMultipleSelection: false
        ) { result in
            guard case let .success(urls) = result, let url = urls.first else {
                if case let .failure(error) = result {
                    viewModel.backupExportFinished(.failure(error))
                }
                return
            }
            Task { await viewModel.restoreBackup(from: url, in: modelContext) }
        }
    }

    private var matchingPreset: PrayerLocationPreset? {
        PrayerLocationPreset.allCases.first(where: {
            $0.configuration.city == viewModel.configuration.city
        })
    }
}

private extension AppearanceMode {
    var title: String {
        switch self {
        case .system: "تلقائي"
        case .light: "فاتح"
        case .dark: "داكن"
        }
    }
}
