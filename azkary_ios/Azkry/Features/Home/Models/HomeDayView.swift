import Foundation

enum HeaderPhase: Equatable, Sendable {
    case dawn
    case day
    case afternoon
    case dusk
    case night
}

struct HomePrayerFrame: Equatable, Sendable {
    let previous: Prayer
    let upcoming: Prayer
}

enum HomeDayView {
    static func phase(now: Date, times: [Prayer: Date]) -> HeaderPhase {
        guard
            let fajr = times[.fajr],
            let sunrise = times[.sunrise],
            let asr = times[.asr],
            let maghrib = times[.maghrib],
            let isha = times[.isha]
        else { return .night }

        if now < fajr { return .night }
        if now < sunrise { return .dawn }
        if now < asr { return .day }
        if now < maghrib { return .afternoon }
        if now < isha { return .dusk }
        return .night
    }

    static func frame(now: Date, times: [Prayer: Date]) -> HomePrayerFrame {
        let ordered = Prayer.allCases.compactMap { prayer in
            times[prayer].map { (prayer, $0) }
        }
        guard ordered.count >= 2, let first = ordered.first, let last = ordered.last else {
            return HomePrayerFrame(previous: .maghrib, upcoming: .isha)
        }

        guard let nextIndex = ordered.firstIndex(where: { now < $0.1 }) else {
            return HomePrayerFrame(previous: last.0, upcoming: first.0)
        }
        guard nextIndex > 0 else {
            return HomePrayerFrame(previous: last.0, upcoming: first.0)
        }
        return HomePrayerFrame(
            previous: ordered[nextIndex - 1].0,
            upcoming: ordered[nextIndex].0
        )
    }

    static func suggestedAdhkarKey(for phase: HeaderPhase) -> String {
        switch phase {
        case .dawn, .day: "morning"
        case .afternoon, .dusk: "evening"
        case .night: "sleep"
        }
    }

    static func countdownPhrase(seconds rawSeconds: TimeInterval) -> String {
        let seconds = max(0, Int(rawSeconds))
        guard seconds >= 60 else { return "أقل من دقيقة" }
        let totalMinutes = (seconds + 59) / 60
        let hours = totalMinutes / 60
        let minutes = totalMinutes % 60
        let hoursPart: String? = switch hours {
        case 0: nil
        case 1: "ساعة"
        case 2: "ساعتين"
        case 3...10: "\(hours) ساعات"
        default: "\(hours) ساعة"
        }
        let minutesPart: String? = switch minutes {
        case 0: nil
        case 1: "دقيقة"
        case 2: "دقيقتين"
        case 3...10: "\(minutes) دقائق"
        default: "\(minutes) دقيقة"
        }
        return [hoursPart, minutesPart].compactMap { $0 }.joined(separator: " و")
    }

    static func countdownClock(seconds rawSeconds: TimeInterval) -> String {
        let total = max(0, Int(rawSeconds))
        let hours = total / 3_600
        let minutes = total % 3_600 / 60
        let seconds = total % 60
        if hours > 0 {
            return String(format: "%d:%02d:%02d", locale: Locale(identifier: "en_US_POSIX"), hours, minutes, seconds)
        }
        return String(format: "%d:%02d", locale: Locale(identifier: "en_US_POSIX"), minutes, seconds)
    }
}
