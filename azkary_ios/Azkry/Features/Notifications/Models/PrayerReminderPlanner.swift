import Foundation

struct PrayerReminderCandidate: Equatable, Sendable {
    let prayer: Prayer
    let date: Date
    let timeZone: TimeZone
}

enum PrayerReminderPlanner {
    static func next(
        after now: Date,
        schedules: [PrayerSchedule]
    ) -> PrayerReminderCandidate? {
        schedules.flatMap { schedule in
            Prayer.obligatory.compactMap { prayer in
                guard let date = schedule.times[prayer], date > now else { return nil }
                return PrayerReminderCandidate(
                    prayer: prayer,
                    date: date,
                    timeZone: schedule.timeZone
                )
            }
        }.min(by: { $0.date < $1.date })
    }
}
