import Foundation

protocol PrayerScheduleServiceProtocol {
    func schedule(containing date: Date) throws -> PrayerSchedule
}
