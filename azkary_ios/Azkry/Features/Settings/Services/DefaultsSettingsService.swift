import Foundation

struct DefaultsSettingsService: SettingsServiceProtocol, @unchecked Sendable {
    private let defaults: UserDefaults

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    func prayerConfiguration() -> PrayerConfiguration {
        let fallback = PrayerConfiguration.mecca
        return PrayerConfiguration(
            city: defaults.string(forKey: StorageKeys.prayerCity) ?? fallback.city,
            location: GeoLocation(
                latitude: defaults.object(forKey: StorageKeys.prayerLatitude) as? Double ?? fallback.location.latitude,
                longitude: defaults.object(forKey: StorageKeys.prayerLongitude) as? Double ?? fallback.location.longitude
            ),
            timeZoneIdentifier: defaults.string(forKey: StorageKeys.prayerTimeZone) ?? fallback.timeZoneIdentifier,
            calculationMethod: defaults.string(forKey: StorageKeys.calculationMethod).flatMap(CalculationMethod.init(rawValue:)) ?? fallback.calculationMethod,
            asrMadhab: defaults.string(forKey: StorageKeys.asrMadhab).flatMap(AsrMadhab.init(rawValue:)) ?? fallback.asrMadhab,
            highLatitudeRule: defaults.string(forKey: StorageKeys.highLatitudeRule).flatMap(HighLatitudeRule.init(rawValue:)) ?? fallback.highLatitudeRule
        )
    }

    func savePrayerConfiguration(_ configuration: PrayerConfiguration) {
        defaults.set(configuration.city, forKey: StorageKeys.prayerCity)
        defaults.set(configuration.location.latitude, forKey: StorageKeys.prayerLatitude)
        defaults.set(configuration.location.longitude, forKey: StorageKeys.prayerLongitude)
        defaults.set(configuration.timeZoneIdentifier, forKey: StorageKeys.prayerTimeZone)
        defaults.set(configuration.calculationMethod.rawValue, forKey: StorageKeys.calculationMethod)
        defaults.set(configuration.asrMadhab.rawValue, forKey: StorageKeys.asrMadhab)
        defaults.set(configuration.highLatitudeRule.rawValue, forKey: StorageKeys.highLatitudeRule)
    }
}
