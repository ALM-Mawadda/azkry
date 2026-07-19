import Foundation
import Observation
import SwiftData

@MainActor
@Observable
final class SettingsViewModel {
    private(set) var configuration = PrayerConfiguration.mecca
    private(set) var savedMessage: String?
    private(set) var remindersEnabled = false
    private(set) var reminderError: String?
    private(set) var isUpdatingReminders = false
    private(set) var isLocating = false
    private(set) var locationError: String?
    private(set) var backupDocument: AzkryBackupDocument?
    private(set) var backupError: String?
    private let service: any SettingsServiceProtocol
    private let reminderService: any PrayerReminderServiceProtocol
    private let locationService: any DeviceLocationServiceProtocol
    private let backupService: any BackupServiceProtocol

    init(
        service: any SettingsServiceProtocol = DefaultsSettingsService(),
        reminderService: any PrayerReminderServiceProtocol = LocalPrayerReminderService(),
        locationService: any DeviceLocationServiceProtocol = DeviceLocationService(),
        backupService: any BackupServiceProtocol = JSONBackupService()
    ) {
        self.service = service
        self.reminderService = reminderService
        self.locationService = locationService
        self.backupService = backupService
    }

    func load() async {
        configuration = service.prayerConfiguration()
        remindersEnabled = await reminderService.isEnabled()
    }

    func apply(_ preset: PrayerLocationPreset) async {
        configuration = preset.configuration
        service.savePrayerConfiguration(configuration)
        savedMessage = "تم اعتماد مواقيت \(configuration.city)."
        locationError = nil
        await reminderService.rescheduleIfEnabled()
    }

    func useCurrentLocation() async {
        guard !isLocating else { return }
        isLocating = true
        defer { isLocating = false }
        do {
            let location = try await locationService.currentLocation()
            configuration = PrayerConfiguration(
                city: "موقعي الحالي",
                location: location,
                timeZoneIdentifier: TimeZone.current.identifier,
                calculationMethod: configuration.calculationMethod,
                asrMadhab: configuration.asrMadhab,
                highLatitudeRule: configuration.highLatitudeRule
            )
            service.savePrayerConfiguration(configuration)
            savedMessage = "تم اعتماد موقعك الحالي لمواقيت الصلاة والقبلة."
            locationError = nil
            await reminderService.rescheduleIfEnabled()
        } catch is CancellationError {
            return
        } catch {
            locationError = error.localizedDescription
        }
    }

    func setRemindersEnabled(_ enabled: Bool) async {
        isUpdatingReminders = true
        defer { isUpdatingReminders = false }
        do {
            try await reminderService.setEnabled(enabled)
            remindersEnabled = enabled
            reminderError = nil
        } catch {
            remindersEnabled = false
            reminderError = error.localizedDescription
        }
    }

    func prepareBackup(in context: ModelContext) -> Bool {
        do {
            backupDocument = AzkryBackupDocument(data: try backupService.exportData(in: context))
            backupError = nil
            return true
        } catch {
            backupError = error.localizedDescription
            return false
        }
    }

    func backupExportFinished(_ result: Result<URL, any Error>) {
        switch result {
        case .success:
            savedMessage = "تم حفظ النسخة الاحتياطية."
            backupError = nil
        case let .failure(error):
            backupError = error.localizedDescription
        }
    }

    func restoreBackup(from url: URL, in context: ModelContext) async {
        do {
            try backupService.restore(from: url, in: context)
            configuration = service.prayerConfiguration()
            remindersEnabled = await reminderService.isEnabled()
            await reminderService.rescheduleIfEnabled()
            savedMessage = "تمت استعادة النسخة الاحتياطية بنجاح."
            backupError = nil
        } catch is CancellationError {
            return
        } catch {
            backupError = error.localizedDescription
        }
    }
}
