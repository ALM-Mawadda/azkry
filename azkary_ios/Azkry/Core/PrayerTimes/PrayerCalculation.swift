import Foundation

struct GeoLocation: Codable, Hashable, Sendable {
    let latitude: Double
    let longitude: Double
}

struct PrayerClockTime: Equatable, Sendable {
    let hour: Int
    let minute: Int
}

struct PrayerTimes: Equatable, Sendable {
    let times: [Prayer: PrayerClockTime]

    subscript(prayer: Prayer) -> PrayerClockTime? { times[prayer] }
}

enum CalculationMethod: String, CaseIterable, Codable, Hashable, Sendable {
    case muslimWorldLeague
    case egyptian
    case ummAlQura
    case karachi
    case northAmerica
    case france15
    case france12

    var fajrAngle: Double {
        switch self {
        case .muslimWorldLeague, .karachi: 18
        case .egyptian: 19.5
        case .ummAlQura: 18.5
        case .northAmerica, .france15: 15
        case .france12: 12
        }
    }

    var ishaAngle: Double? {
        switch self {
        case .muslimWorldLeague: 17
        case .egyptian: 17.5
        case .ummAlQura: nil
        case .karachi: 18
        case .northAmerica, .france15: 15
        case .france12: 12
        }
    }

    var ishaMinutesAfterMaghrib: Int? {
        self == .ummAlQura ? 90 : nil
    }
}

enum AsrMadhab: String, CaseIterable, Codable, Hashable, Sendable {
    case shafii
    case hanafi

    var shadowFactor: Double { self == .shafii ? 1 : 2 }
}

enum HighLatitudeRule: String, CaseIterable, Codable, Hashable, Sendable {
    case none
    case middleOfTheNight
    case seventhOfTheNight
    case angleBased
}

enum PrayerTimeCalculationError: Error, Equatable {
    case invalidDate
    case undefinedPrayerTime(Prayer)
}

/// Pure astronomical calculation ported from the Android implementation.
/// It uses the standard approximate solar ephemeris found in established
/// prayer-time libraries and intentionally has no UIKit or persistence imports.
enum PrayerTimeCalculator {
    private static let sunriseSunsetAngle = 0.833

    static func calculate(
        date: DateComponents,
        location: GeoLocation,
        utcOffsetHours: Double,
        method: CalculationMethod,
        asrMadhab: AsrMadhab = .shafii,
        highLatitudeRule: HighLatitudeRule = .angleBased
    ) throws -> PrayerTimes {
        guard let year = date.year, let month = date.month, let day = date.day else {
            throw PrayerTimeCalculationError.invalidDate
        }
        let julianBase = julianDay(year: year, month: month, day: day)

        var fajr = 5.0
        var sunrise = 6.0
        var dhuhr = 12.0
        var asr = 13.0
        var maghrib = 18.0
        var isha = 18.0

        for _ in 0..<2 {
            fajr = solarTimeForAngle(
                julianBase: julianBase,
                estimate: fajr,
                location: location,
                angle: method.fajrAngle,
                before: true
            )
            sunrise = solarTimeForAngle(
                julianBase: julianBase,
                estimate: sunrise,
                location: location,
                angle: sunriseSunsetAngle,
                before: true
            )
            dhuhr = midDay(julianBase: julianBase, estimate: dhuhr)
            asr = asrTime(
                julianBase: julianBase,
                estimate: asr,
                location: location,
                madhab: asrMadhab
            )
            maghrib = solarTimeForAngle(
                julianBase: julianBase,
                estimate: maghrib,
                location: location,
                angle: sunriseSunsetAngle,
                before: false
            )
            if let ishaAngle = method.ishaAngle {
                isha = solarTimeForAngle(
                    julianBase: julianBase,
                    estimate: isha,
                    location: location,
                    angle: ishaAngle,
                    before: false
                )
            } else {
                isha = maghrib + Double(method.ishaMinutesAfterMaghrib ?? 0) / 60
            }
        }

        // During polar day/night the sun never crosses the horizon, so the
        // ordinary sunrise and sunset values are undefined. Use the widely
        // adopted nearest-latitude convention as the civil-day frame, then
        // apply the selected high-latitude rule to Fajr and Isha below.
        if highLatitudeRule != .none, !sunrise.isFinite || !maghrib.isFinite {
            let referenceLatitude = min(abs(location.latitude), 48.5)
                * (location.latitude < 0 ? -1 : 1)
            let referenceLocation = GeoLocation(
                latitude: referenceLatitude,
                longitude: location.longitude
            )
            sunrise = referenceSolarTime(
                julianBase: julianBase,
                estimate: 6,
                location: referenceLocation,
                angle: sunriseSunsetAngle,
                before: true
            )
            maghrib = referenceSolarTime(
                julianBase: julianBase,
                estimate: 18,
                location: referenceLocation,
                angle: sunriseSunsetAngle,
                before: false
            )
            if !asr.isFinite {
                asr = asrTime(
                    julianBase: julianBase,
                    estimate: 13,
                    location: referenceLocation,
                    madhab: asrMadhab
                )
            }
            if method.ishaAngle == nil {
                isha = maghrib + Double(method.ishaMinutesAfterMaghrib ?? 0) / 60
            }
        }

        if highLatitudeRule != .none {
            let night = fixHour(sunrise - maghrib)
            fajr = adjustForHighLatitude(
                time: fajr,
                base: sunrise,
                angle: method.fajrAngle,
                night: night,
                rule: highLatitudeRule,
                before: true
            )
            if let ishaAngle = method.ishaAngle {
                isha = adjustForHighLatitude(
                    time: isha,
                    base: maghrib,
                    angle: ishaAngle,
                    night: night,
                    rule: highLatitudeRule,
                    before: false
                )
            }
        }

        let solarToLocal = utcOffsetHours - location.longitude / 15
        let rawTimes: [(Prayer, Double)] = [
            (.fajr, fajr),
            (.sunrise, sunrise),
            (.dhuhr, dhuhr),
            (.asr, asr),
            (.maghrib, maghrib),
            (.isha, isha),
        ]

        var result: [Prayer: PrayerClockTime] = [:]
        for (prayer, solarHours) in rawTimes {
            guard solarHours.isFinite else {
                throw PrayerTimeCalculationError.undefinedPrayerTime(prayer)
            }
            let local = fixHour(solarHours + solarToLocal)
            let totalMinutes = Int(local * 60 + 0.5) % (24 * 60)
            result[prayer] = PrayerClockTime(
                hour: totalMinutes / 60,
                minute: totalMinutes % 60
            )
        }

        return PrayerTimes(times: result)
    }

    private static func julianDay(year initialYear: Int, month initialMonth: Int, day: Int) -> Double {
        var year = initialYear
        var month = initialMonth
        if month <= 2 {
            year -= 1
            month += 12
        }
        let a = floor(Double(year) / 100)
        let b = 2 - a + floor(a / 4)
        return floor(365.25 * Double(year + 4716))
            + floor(30.6001 * Double(month + 1))
            + Double(day) + b - 1524.5
    }

    private static func sunPosition(julianDay: Double) -> (declination: Double, equationOfTime: Double) {
        let days = julianDay - 2_451_545
        let anomaly = fixAngle(357.529 + 0.98560028 * days)
        let longitudeBase = fixAngle(280.459 + 0.98564736 * days)
        let longitude = fixAngle(
            longitudeBase + 1.915 * sinDegrees(anomaly) + 0.020 * sinDegrees(2 * anomaly)
        )
        let obliquity = 23.439 - 0.00000036 * days
        let declination = radiansToDegrees(
            asin(sinDegrees(obliquity) * sinDegrees(longitude))
        )
        let rightAscension = fixHour(
            radiansToDegrees(
                atan2(cosDegrees(obliquity) * sinDegrees(longitude), cosDegrees(longitude))
            ) / 15
        )
        let equation = longitudeBase / 15 - rightAscension
        return (declination, normalizeEquationOfTime(equation))
    }

    private static func normalizeEquationOfTime(_ value: Double) -> Double {
        if value > 12 { return value - 24 }
        if value < -12 { return value + 24 }
        return value
    }

    private static func midDay(julianBase: Double, estimate: Double) -> Double {
        let position = sunPosition(julianDay: julianBase + estimate / 24)
        return fixHour(12 - position.equationOfTime)
    }

    private static func solarTimeForAngle(
        julianBase: Double,
        estimate: Double,
        location: GeoLocation,
        angle: Double,
        before: Bool
    ) -> Double {
        let declination = sunPosition(julianDay: julianBase + estimate / 24).declination
        let midday = midDay(julianBase: julianBase, estimate: estimate)
        let cosineHourAngle = (
            -sinDegrees(angle) - sinDegrees(declination) * sinDegrees(location.latitude)
        ) / (cosDegrees(declination) * cosDegrees(location.latitude))
        guard (-1...1).contains(cosineHourAngle) else { return .nan }
        let hourAngle = radiansToDegrees(acos(cosineHourAngle)) / 15
        return before ? midday - hourAngle : midday + hourAngle
    }

    private static func referenceSolarTime(
        julianBase: Double,
        estimate: Double,
        location: GeoLocation,
        angle: Double,
        before: Bool
    ) -> Double {
        var result = estimate
        for _ in 0..<2 {
            result = solarTimeForAngle(
                julianBase: julianBase,
                estimate: result,
                location: location,
                angle: angle,
                before: before
            )
        }
        return result
    }

    private static func asrTime(
        julianBase: Double,
        estimate: Double,
        location: GeoLocation,
        madhab: AsrMadhab
    ) -> Double {
        let declination = sunPosition(julianDay: julianBase + estimate / 24).declination
        let midday = midDay(julianBase: julianBase, estimate: estimate)
        let altitude = radiansToDegrees(
            atan(1 / (madhab.shadowFactor + tanDegrees(abs(location.latitude - declination))))
        )
        let cosineHourAngle = (
            sinDegrees(altitude) - sinDegrees(declination) * sinDegrees(location.latitude)
        ) / (cosDegrees(declination) * cosDegrees(location.latitude))
        guard (-1...1).contains(cosineHourAngle) else { return .nan }
        return midday + radiansToDegrees(acos(cosineHourAngle)) / 15
    }

    private static func adjustForHighLatitude(
        time: Double,
        base: Double,
        angle: Double,
        night: Double,
        rule: HighLatitudeRule,
        before: Bool
    ) -> Double {
        let portion: Double
        switch rule {
        case .middleOfTheNight: portion = night / 2
        case .seventhOfTheNight: portion = night / 7
        case .angleBased: portion = angle / 60 * night
        case .none: return time
        }
        let difference = before ? fixHour(base - time) : fixHour(time - base)
        if time.isNaN || difference > portion {
            return before ? base - portion : base + portion
        }
        return time
    }

    private static func fixAngle(_ angle: Double) -> Double {
        positiveRemainder(angle, modulus: 360)
    }

    private static func fixHour(_ hour: Double) -> Double {
        positiveRemainder(hour, modulus: 24)
    }

    private static func positiveRemainder(_ value: Double, modulus: Double) -> Double {
        let remainder = value.truncatingRemainder(dividingBy: modulus)
        return remainder < 0 ? remainder + modulus : remainder
    }

    private static func radiansToDegrees(_ radians: Double) -> Double {
        radians * 180 / .pi
    }

    private static func sinDegrees(_ degrees: Double) -> Double { sin(degrees * .pi / 180) }
    private static func cosDegrees(_ degrees: Double) -> Double { cos(degrees * .pi / 180) }
    private static func tanDegrees(_ degrees: Double) -> Double { tan(degrees * .pi / 180) }
}
