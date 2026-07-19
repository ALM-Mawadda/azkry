import Foundation
import Observation

@MainActor
@Observable
final class PrayerTimesViewModel {
    private(set) var schedule: PrayerSchedule?
    private(set) var errorMessage: String?
    private let service: any PrayerScheduleServiceProtocol
    private let formatter: DateFormatter

    init(service: (any PrayerScheduleServiceProtocol)? = nil) {
        self.service = service ?? CalculatedPrayerScheduleService()
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.dateFormat = "h:mm"
        self.formatter = formatter
    }

    func load(date: Date = .now) {
        do {
            schedule = try service.schedule(containing: date)
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func displayTime(for prayer: Prayer) -> String {
        guard let schedule, let date = schedule.times[prayer] else { return "--:--" }
        formatter.timeZone = schedule.timeZone
        return formatter.string(from: date)
    }
}
