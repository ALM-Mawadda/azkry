import Foundation
import Testing
@testable import Azkry

@Suite("Features/CalculatedPrayerScheduleService")
struct CalculatedPrayerScheduleServiceTests {
    @Test("DST transition day uses the target date's noon civil offset")
    func dstTransitionUsesNoonOffset() throws {
        let suiteName = "AzkryTests.\(UUID().uuidString)"
        let defaults = UserDefaults(suiteName: suiteName)!
        defer { defaults.removePersistentDomain(forName: suiteName) }
        defaults.set(40.7128, forKey: StorageKeys.prayerLatitude)
        defaults.set(-74.0060, forKey: StorageKeys.prayerLongitude)
        defaults.set("America/New_York", forKey: StorageKeys.prayerTimeZone)
        defaults.set(CalculationMethod.northAmerica.rawValue, forKey: StorageKeys.calculationMethod)
        defaults.set(AsrMadhab.shafii.rawValue, forKey: StorageKeys.asrMadhab)
        defaults.set(HighLatitudeRule.angleBased.rawValue, forKey: StorageKeys.highLatitudeRule)

        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = try #require(TimeZone(identifier: "America/New_York"))
        let justAfterMidnight = try #require(
            calendar.date(
                from: DateComponents(
                    year: 2026,
                    month: 3,
                    day: 8,
                    hour: 0,
                    minute: 30
                )
            )
        )
        let service = CalculatedPrayerScheduleService(defaults: defaults)
        let schedule = try service.schedule(containing: justAfterMidnight)
        let expected = try PrayerTimeCalculator.calculate(
            date: DateComponents(year: 2026, month: 3, day: 8),
            location: GeoLocation(latitude: 40.7128, longitude: -74.0060),
            utcOffsetHours: -4,
            method: .northAmerica,
            asrMadhab: .shafii,
            highLatitudeRule: .angleBased
        )

        for prayer in Prayer.allCases {
            let actualDate = try #require(schedule.times[prayer])
            let components = calendar.dateComponents([.hour, .minute], from: actualDate)
            let expectedTime = try #require(expected[prayer])
            #expect(components.hour == expectedTime.hour)
            #expect(components.minute == expectedTime.minute)
        }
    }
}
