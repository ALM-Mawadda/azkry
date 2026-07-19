import Foundation

enum PrayerScheduleServiceError: LocalizedError {
    case invalidTimeZone(String)
    case invalidPrayerDate(Prayer)

    var errorDescription: String? {
        switch self {
        case .invalidTimeZone(let identifier):
            "المنطقة الزمنية غير صالحة: \(identifier)."
        case .invalidPrayerDate(let prayer):
            "تعذر حساب وقت \(prayer.title) لهذا اليوم."
        }
    }
}

final class CalculatedPrayerScheduleService: PrayerScheduleServiceProtocol {
    private struct CacheKey: Hashable {
        let year: Int
        let month: Int
        let day: Int
        let configuration: PrayerConfiguration
    }

    private let defaults: UserDefaults
    private var cachedSchedule: (key: CacheKey, value: PrayerSchedule)?

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    func schedule(containing date: Date) throws -> PrayerSchedule {
        let configuration = loadConfiguration()
        guard let timeZone = TimeZone(identifier: configuration.timeZoneIdentifier) else {
            throw PrayerScheduleServiceError.invalidTimeZone(configuration.timeZoneIdentifier)
        }

        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = timeZone
        let day = calendar.dateComponents([.year, .month, .day], from: date)
        guard
            let year = day.year,
            let month = day.month,
            let dayOfMonth = day.day
        else {
            throw PrayerTimeCalculationError.invalidDate
        }
        let cacheKey = CacheKey(
            year: year,
            month: month,
            day: dayOfMonth,
            configuration: configuration
        )
        if let cachedSchedule, cachedSchedule.key == cacheKey {
            return cachedSchedule.value
        }

        var noonComponents = day
        noonComponents.calendar = calendar
        noonComponents.timeZone = timeZone
        noonComponents.hour = 12
        guard let localNoon = calendar.date(from: noonComponents) else {
            throw PrayerTimeCalculationError.invalidDate
        }
        let utcOffset = Double(timeZone.secondsFromGMT(for: localNoon)) / 3_600
        let calculated = try PrayerTimeCalculator.calculate(
            date: day,
            location: configuration.location,
            utcOffsetHours: utcOffset,
            method: configuration.calculationMethod,
            asrMadhab: configuration.asrMadhab,
            highLatitudeRule: configuration.highLatitudeRule
        )

        var times: [Prayer: Date] = [:]
        for prayer in Prayer.allCases {
            guard let clock = calculated[prayer] else {
                throw PrayerScheduleServiceError.invalidPrayerDate(prayer)
            }
            var components = day
            components.calendar = calendar
            components.timeZone = timeZone
            components.hour = clock.hour
            components.minute = clock.minute
            components.second = 0
            guard let prayerDate = calendar.date(from: components) else {
                throw PrayerScheduleServiceError.invalidPrayerDate(prayer)
            }
            times[prayer] = prayerDate
        }

        let schedule = PrayerSchedule(
            city: configuration.city,
            timeZone: timeZone,
            times: times
        )
        cachedSchedule = (cacheKey, schedule)
        return schedule
    }

    private func loadConfiguration() -> PrayerConfiguration {
        let fallback = PrayerConfiguration.mecca
        let latitude = defaults.object(forKey: StorageKeys.prayerLatitude) as? Double
            ?? fallback.location.latitude
        let longitude = defaults.object(forKey: StorageKeys.prayerLongitude) as? Double
            ?? fallback.location.longitude
        let method = defaults.string(forKey: StorageKeys.calculationMethod)
            .flatMap(CalculationMethod.init(rawValue:)) ?? fallback.calculationMethod
        let madhab = defaults.string(forKey: StorageKeys.asrMadhab)
            .flatMap(AsrMadhab.init(rawValue:)) ?? fallback.asrMadhab
        let highLatitude = defaults.string(forKey: StorageKeys.highLatitudeRule)
            .flatMap(HighLatitudeRule.init(rawValue:)) ?? fallback.highLatitudeRule

        return PrayerConfiguration(
            city: defaults.string(forKey: StorageKeys.prayerCity) ?? fallback.city,
            location: GeoLocation(latitude: latitude, longitude: longitude),
            timeZoneIdentifier: defaults.string(forKey: StorageKeys.prayerTimeZone)
                ?? fallback.timeZoneIdentifier,
            calculationMethod: method,
            asrMadhab: madhab,
            highLatitudeRule: highLatitude
        )
    }
}
