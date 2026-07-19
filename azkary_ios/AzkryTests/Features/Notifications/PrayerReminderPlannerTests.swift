import Foundation
import Testing
@testable import Azkry

@Suite("Features/PrayerReminderPlanner")
struct PrayerReminderPlannerTests {
    @Test("Planner skips sunrise and chooses the next obligatory prayer")
    func nextObligatoryPrayer() throws {
        let timeZone = try #require(TimeZone(identifier: "Europe/Paris"))
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = timeZone
        let base = try #require(calendar.date(from: DateComponents(year: 2026, month: 7, day: 19)))
        let now = try #require(calendar.date(byAdding: .hour, value: 6, to: base))
        let schedule = PrayerSchedule(
            city: "باريس",
            timeZone: timeZone,
            times: [
                .fajr: try #require(calendar.date(byAdding: .hour, value: 4, to: base)),
                .sunrise: try #require(calendar.date(byAdding: .hour, value: 7, to: base)),
                .dhuhr: try #require(calendar.date(byAdding: .hour, value: 13, to: base)),
                .asr: try #require(calendar.date(byAdding: .hour, value: 17, to: base)),
                .maghrib: try #require(calendar.date(byAdding: .hour, value: 21, to: base)),
                .isha: try #require(calendar.date(byAdding: .hour, value: 23, to: base))
            ]
        )

        let candidate = try #require(
            PrayerReminderPlanner.next(after: now, schedules: [schedule])
        )
        #expect(candidate.prayer == .dhuhr)
    }
}
