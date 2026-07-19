import Foundation
import Testing
@testable import Azkry

@Suite("Core/PrayerTimeCalculator")
struct PrayerTimeCalculatorTests {
    private let mecca = GeoLocation(latitude: 21.4225, longitude: 39.8262)
    private let paris = GeoLocation(latitude: 48.8566, longitude: 2.3522)
    private let oslo = GeoLocation(latitude: 59.9139, longitude: 10.7522)
    private let tromso = GeoLocation(latitude: 69.6492, longitude: 18.9553)

    @Test("Equator equinox anchors sunrise, noon, and sunset")
    func equatorEquinox() throws {
        let times = try PrayerTimeCalculator.calculate(
            date: DateComponents(year: 2026, month: 3, day: 20),
            location: GeoLocation(latitude: 0, longitude: 0),
            utcOffsetHours: 0,
            method: .muslimWorldLeague
        )

        #expect(withinMinutes(try #require(times[.sunrise]), hour: 6, minute: 0, tolerance: 15))
        #expect(withinMinutes(try #require(times[.dhuhr]), hour: 12, minute: 0, tolerance: 15))
        #expect(withinMinutes(try #require(times[.maghrib]), hour: 18, minute: 0, tolerance: 15))
    }

    @Test("Mecca prayer times stay strictly ordered across seasons", arguments: [
        DateComponents(year: 2026, month: 1, day: 15),
        DateComponents(year: 2026, month: 4, day: 15),
        DateComponents(year: 2026, month: 7, day: 17),
        DateComponents(year: 2026, month: 10, day: 15),
    ])
    func meccaOrder(date: DateComponents) throws {
        let times = try PrayerTimeCalculator.calculate(
            date: date,
            location: mecca,
            utcOffsetHours: 3,
            method: .ummAlQura
        )
        let minutes = try Prayer.allCases.map { totalMinutes(try #require(times[$0])) }
        for pair in zip(minutes, minutes.dropFirst()) {
            #expect(pair.0 < pair.1)
        }
    }

    @Test("Hanafi Asr is later than Shafii Asr")
    func hanafiAsrIsLater() throws {
        let date = DateComponents(year: 2026, month: 7, day: 17)
        let shafii = try PrayerTimeCalculator.calculate(
            date: date,
            location: paris,
            utcOffsetHours: 2,
            method: .france15,
            asrMadhab: .shafii
        )
        let hanafi = try PrayerTimeCalculator.calculate(
            date: date,
            location: paris,
            utcOffsetHours: 2,
            method: .france15,
            asrMadhab: .hanafi
        )

        #expect(
            totalMinutes(try #require(hanafi[.asr]))
                > totalMinutes(try #require(shafii[.asr]))
        )
    }

    @Test("Umm al-Qura Isha is ninety minutes after Maghrib")
    func ummAlQuraInterval() throws {
        let times = try PrayerTimeCalculator.calculate(
            date: DateComponents(year: 2026, month: 2, day: 1),
            location: mecca,
            utcOffsetHours: 3,
            method: .ummAlQura
        )
        #expect(
            abs(
                totalMinutes(try #require(times[.isha]))
                    - totalMinutes(try #require(times[.maghrib]))
                    - 90
            ) <= 1
        )
    }

    @Test("High-latitude rule produces finite Oslo midsummer times")
    func highLatitudeFallback() throws {
        let times = try PrayerTimeCalculator.calculate(
            date: DateComponents(year: 2026, month: 6, day: 21),
            location: oslo,
            utcOffsetHours: 2,
            method: .muslimWorldLeague,
            highLatitudeRule: .angleBased
        )

        #expect(
            totalMinutes(try #require(times[.fajr]))
                < totalMinutes(try #require(times[.sunrise]))
        )
        #expect(times.times.count == Prayer.allCases.count)
    }

    @Test("Polar-day fallback produces a complete ordered Tromsø schedule")
    func polarDayFallback() throws {
        let times = try PrayerTimeCalculator.calculate(
            date: DateComponents(year: 2026, month: 6, day: 21),
            location: tromso,
            utcOffsetHours: 2,
            method: .muslimWorldLeague,
            highLatitudeRule: .angleBased
        )

        let minutes = try Prayer.allCases.map { totalMinutes(try #require(times[$0])) }
        #expect(times.times.count == Prayer.allCases.count)
        for pair in zip(minutes, minutes.dropFirst()) {
            #expect(pair.0 < pair.1)
        }
    }

    private func totalMinutes(_ time: PrayerClockTime) -> Int {
        time.hour * 60 + time.minute
    }

    private func withinMinutes(
        _ time: PrayerClockTime,
        hour: Int,
        minute: Int,
        tolerance: Int
    ) -> Bool {
        abs(totalMinutes(time) - (hour * 60 + minute)) <= tolerance
    }
}
