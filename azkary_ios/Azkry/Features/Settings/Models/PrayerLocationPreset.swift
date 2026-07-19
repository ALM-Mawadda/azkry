enum PrayerLocationPreset: String, CaseIterable, Identifiable, Sendable {
    case mecca
    case paris
    case london
    case newYork

    var id: String { rawValue }

    var configuration: PrayerConfiguration {
        switch self {
        case .mecca:
            .mecca
        case .paris:
            PrayerConfiguration(
                city: "باريس",
                location: GeoLocation(latitude: 48.8566, longitude: 2.3522),
                timeZoneIdentifier: "Europe/Paris",
                calculationMethod: .france15,
                asrMadhab: .shafii,
                highLatitudeRule: .angleBased
            )
        case .london:
            PrayerConfiguration(
                city: "لندن",
                location: GeoLocation(latitude: 51.5072, longitude: -0.1276),
                timeZoneIdentifier: "Europe/London",
                calculationMethod: .muslimWorldLeague,
                asrMadhab: .shafii,
                highLatitudeRule: .angleBased
            )
        case .newYork:
            PrayerConfiguration(
                city: "نيويورك",
                location: GeoLocation(latitude: 40.7128, longitude: -74.0060),
                timeZoneIdentifier: "America/New_York",
                calculationMethod: .northAmerica,
                asrMadhab: .shafii,
                highLatitudeRule: .angleBased
            )
        }
    }

    var title: String { configuration.city }
}
