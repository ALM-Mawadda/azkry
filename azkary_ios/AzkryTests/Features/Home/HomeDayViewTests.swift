import Foundation
import Testing
@testable import Azkry

@Suite("Features/HomeDayView")
struct HomeDayViewTests {
    private var times: [Prayer: Date] {
        [
            .fajr: date(hour: 4, minute: 15),
            .sunrise: date(hour: 6, minute: 7),
            .dhuhr: date(hour: 13, minute: 45),
            .asr: date(hour: 17, minute: 30),
            .maghrib: date(hour: 21, minute: 10),
            .isha: date(hour: 22, minute: 40),
        ]
    }

    @Test("Header phases follow prayer events")
    func phases() {
        #expect(HomeDayView.phase(now: date(hour: 2), times: times) == .night)
        #expect(HomeDayView.phase(now: date(hour: 5), times: times) == .dawn)
        #expect(HomeDayView.phase(now: date(hour: 12, minute: 30), times: times) == .day)
        #expect(HomeDayView.phase(now: date(hour: 18), times: times) == .afternoon)
        #expect(HomeDayView.phase(now: date(hour: 21, minute: 30), times: times) == .dusk)
        #expect(HomeDayView.phase(now: date(hour: 23), times: times) == .night)
    }

    @Test("Prayer strip frames now and wraps overnight")
    func frames() {
        #expect(
            HomeDayView.frame(now: date(hour: 12, minute: 30), times: times)
                == HomePrayerFrame(previous: .sunrise, upcoming: .dhuhr)
        )
        #expect(
            HomeDayView.frame(now: date(hour: 21, minute: 30), times: times)
                == HomePrayerFrame(previous: .maghrib, upcoming: .isha)
        )
        #expect(
            HomeDayView.frame(now: date(hour: 23, minute: 30), times: times)
                == HomePrayerFrame(previous: .isha, upcoming: .fajr)
        )
        #expect(
            HomeDayView.frame(now: date(hour: 3), times: times)
                == HomePrayerFrame(previous: .isha, upcoming: .fajr)
        )
    }

    @Test("Countdown phrase rounds up and clock remains precise")
    func countdownFormatting() {
        #expect(HomeDayView.countdownPhrase(seconds: 188) == "4 دقائق")
        #expect(HomeDayView.countdownPhrase(seconds: 45) == "أقل من دقيقة")
        #expect(HomeDayView.countdownPhrase(seconds: 3_600) == "ساعة")
        #expect(HomeDayView.countdownPhrase(seconds: 6_970) == "ساعة و57 دقيقة")
        #expect(HomeDayView.countdownClock(seconds: 188) == "3:08")
        #expect(HomeDayView.countdownClock(seconds: 6_970) == "1:56:10")
        #expect(HomeDayView.countdownClock(seconds: -5) == "0:00")
    }

    private func date(hour: Int, minute: Int = 0) -> Date {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(secondsFromGMT: 0)!
        return calendar.date(
            from: DateComponents(
                timeZone: calendar.timeZone,
                year: 2026,
                month: 7,
                day: 17,
                hour: hour,
                minute: minute
            )
        )!
    }
}
