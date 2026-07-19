import Foundation
import Observation

struct HomeViewState: Equatable {
    var city = PrayerConfiguration.mecca.city
    var phase: HeaderPhase = .night
    var previousPrayer: Prayer = .maghrib
    var previousTime = "--:--"
    var upcomingPrayer: Prayer = .isha
    var upcomingTime = "--:--"
    var hijriDay = 1
    var hijriMonth = "محرم"
    var countdownPhrase = "—"
    var countdownClock = "--:--"
    var suggestedAdhkarTitle = "أذكار النوم"
    var isFriday = false
    var errorMessage: String?
}

@MainActor
@Observable
final class HomeViewModel {
    private(set) var state = HomeViewState()

    private let prayerService: any PrayerScheduleServiceProtocol
    private let timeFormatter: DateFormatter
    private let hijriMonthFormatter: DateFormatter

    init(prayerService: (any PrayerScheduleServiceProtocol)? = nil) {
        self.prayerService = prayerService ?? CalculatedPrayerScheduleService()
        let timeFormatter = DateFormatter()
        timeFormatter.locale = Locale(identifier: "en_US_POSIX")
        timeFormatter.dateFormat = "h:mm"
        self.timeFormatter = timeFormatter

        let hijriMonthFormatter = DateFormatter()
        hijriMonthFormatter.locale = Locale(identifier: "ar")
        hijriMonthFormatter.calendar = Calendar(identifier: .islamicUmmAlQura)
        hijriMonthFormatter.dateFormat = "MMMM"
        self.hijriMonthFormatter = hijriMonthFormatter
    }

    /// Attached to the view's `.task`, so SwiftUI cancellation ends the ticker
    /// when the screen leaves the hierarchy.
    func run() async {
        while !Task.isCancelled {
            refresh(at: .now)
            do {
                try await Task.sleep(for: .seconds(1))
            } catch {
                return
            }
        }
    }

    func refresh(at now: Date) {
        do {
            let schedule = try prayerService.schedule(containing: now)
            let frame = HomeDayView.frame(now: now, times: schedule.times)
            let phase = HomeDayView.phase(now: now, times: schedule.times)
            let target = try upcomingTarget(
                now: now,
                frame: frame,
                schedule: schedule
            )
            let remaining = target.timeIntervalSince(now)
            let hijri = hijriDate(for: now, timeZone: schedule.timeZone)

            state = HomeViewState(
                city: schedule.city,
                phase: phase,
                previousPrayer: frame.previous,
                previousTime: displayTime(
                    schedule.times[frame.previous],
                    timeZone: schedule.timeZone
                ),
                upcomingPrayer: frame.upcoming,
                upcomingTime: displayTime(target, timeZone: schedule.timeZone),
                hijriDay: hijri.day,
                hijriMonth: hijri.month,
                countdownPhrase: HomeDayView.countdownPhrase(seconds: remaining),
                countdownClock: HomeDayView.countdownClock(seconds: remaining),
                suggestedAdhkarTitle: suggestedTitle(for: phase),
                isFriday: weekday(for: now, timeZone: schedule.timeZone) == 6,
                errorMessage: nil
            )
        } catch {
            state.errorMessage = error.localizedDescription
        }
    }

    private func upcomingTarget(
        now: Date,
        frame: HomePrayerFrame,
        schedule: PrayerSchedule
    ) throws -> Date {
        guard let sameDayTarget = schedule.times[frame.upcoming] else {
            throw PrayerScheduleServiceError.invalidPrayerDate(frame.upcoming)
        }
        if frame.upcoming != .fajr || now < sameDayTarget {
            return sameDayTarget
        }

        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = schedule.timeZone
        guard let tomorrow = calendar.date(byAdding: .day, value: 1, to: now) else {
            throw PrayerTimeCalculationError.invalidDate
        }
        let tomorrowSchedule = try prayerService.schedule(containing: tomorrow)
        guard let fajr = tomorrowSchedule.times[.fajr] else {
            throw PrayerScheduleServiceError.invalidPrayerDate(.fajr)
        }
        return fajr
    }

    private func displayTime(_ date: Date?, timeZone: TimeZone) -> String {
        guard let date else { return "--:--" }
        timeFormatter.timeZone = timeZone
        return timeFormatter.string(from: date)
    }

    private func hijriDate(for date: Date, timeZone: TimeZone) -> (day: Int, month: String) {
        var calendar = Calendar(identifier: .islamicUmmAlQura)
        calendar.timeZone = timeZone
        let day = calendar.component(.day, from: date)
        hijriMonthFormatter.calendar = calendar
        hijriMonthFormatter.timeZone = timeZone
        return (day, hijriMonthFormatter.string(from: date))
    }

    private func weekday(for date: Date, timeZone: TimeZone) -> Int {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = timeZone
        return calendar.component(.weekday, from: date)
    }

    private func suggestedTitle(for phase: HeaderPhase) -> String {
        switch HomeDayView.suggestedAdhkarKey(for: phase) {
        case "morning": "أذكار الصباح"
        case "evening": "أذكار المساء"
        default: "أذكار النوم"
        }
    }
}
