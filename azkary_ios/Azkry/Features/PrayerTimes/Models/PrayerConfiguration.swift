import Foundation

struct PrayerConfiguration: Codable, Hashable, Sendable {
    let city: String
    let location: GeoLocation
    let timeZoneIdentifier: String
    let calculationMethod: CalculationMethod
    let asrMadhab: AsrMadhab
    let highLatitudeRule: HighLatitudeRule

    static let mecca = PrayerConfiguration(
        city: "مكة المكرمة",
        location: GeoLocation(latitude: 21.4225, longitude: 39.8262),
        timeZoneIdentifier: "Asia/Riyadh",
        calculationMethod: .ummAlQura,
        asrMadhab: .shafii,
        highLatitudeRule: .angleBased
    )
}

struct PrayerSchedule: Equatable, Sendable {
    let city: String
    let timeZone: TimeZone
    let times: [Prayer: Date]
}
