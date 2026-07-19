import Foundation
import UserNotifications

enum PrayerReminderError: LocalizedError {
    case permissionDenied
    case noUpcomingPrayer

    var errorDescription: String? {
        switch self {
        case .permissionDenied:
            "لم يُسمح لأذكاري بإرسال التنبيهات. يمكنك تغيير الإذن من إعدادات iPhone."
        case .noUpcomingPrayer:
            "تعذر تحديد الصلاة القادمة للتنبيه."
        }
    }
}

@MainActor
final class LocalPrayerReminderService: PrayerReminderServiceProtocol {
    private let center: UNUserNotificationCenter
    private let defaults: UserDefaults
    private let prayerService: any PrayerScheduleServiceProtocol
    private let identifier = "azkry.next-prayer"

    init(
        center: UNUserNotificationCenter = .current(),
        defaults: UserDefaults = .standard,
        prayerService: any PrayerScheduleServiceProtocol = CalculatedPrayerScheduleService()
    ) {
        self.center = center
        self.defaults = defaults
        self.prayerService = prayerService
    }

    func authorization() async -> ReminderAuthorization {
        switch await center.notificationSettings().authorizationStatus {
        case .authorized, .provisional, .ephemeral: .enabled
        case .denied: .denied
        case .notDetermined: .unknown
        @unknown default: .unknown
        }
    }

    func isEnabled() async -> Bool {
        guard defaults.bool(forKey: StorageKeys.prayerRemindersEnabled) else { return false }
        return await authorization() == .enabled
    }

    func setEnabled(_ enabled: Bool) async throws {
        guard enabled else {
            defaults.set(false, forKey: StorageKeys.prayerRemindersEnabled)
            center.removePendingNotificationRequests(withIdentifiers: [identifier])
            return
        }

        let granted = try await center.requestAuthorization(options: [.alert, .sound])
        guard granted else { throw PrayerReminderError.permissionDenied }
        defaults.set(true, forKey: StorageKeys.prayerRemindersEnabled)
        try await scheduleNext(after: .now)
    }

    func rescheduleIfEnabled() async {
        guard defaults.bool(forKey: StorageKeys.prayerRemindersEnabled) else { return }
        try? await scheduleNext(after: .now)
    }

    private func scheduleNext(after now: Date) async throws {
        let today = try prayerService.schedule(containing: now)
        var schedules = [today]
        if PrayerReminderPlanner.next(after: now, schedules: schedules) == nil {
            var calendar = Calendar(identifier: .gregorian)
            calendar.timeZone = today.timeZone
            guard let tomorrow = calendar.date(byAdding: .day, value: 1, to: now) else {
                throw PrayerTimeCalculationError.invalidDate
            }
            schedules.append(try prayerService.schedule(containing: tomorrow))
        }

        guard let candidate = PrayerReminderPlanner.next(after: now, schedules: schedules) else {
            throw PrayerReminderError.noUpcomingPrayer
        }
        let content = UNMutableNotificationContent()
        content.title = "حان وقت صلاة \(candidate.prayer.title)"
        content.body = "حيّ على الصلاة · أذكاري"
        content.sound = .default

        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = candidate.timeZone
        var components = calendar.dateComponents(
            [.calendar, .timeZone, .year, .month, .day, .hour, .minute],
            from: candidate.date
        )
        components.second = 0
        let trigger = UNCalendarNotificationTrigger(dateMatching: components, repeats: false)
        let request = UNNotificationRequest(identifier: identifier, content: content, trigger: trigger)
        center.removePendingNotificationRequests(withIdentifiers: [identifier])
        try await center.add(request)
    }
}
