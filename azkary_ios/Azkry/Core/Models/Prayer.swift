import Foundation

enum Prayer: String, CaseIterable, Codable, Identifiable {
    case fajr
    case sunrise
    case dhuhr
    case asr
    case maghrib
    case isha

    var id: String { rawValue }

    var isObligatory: Bool { self != .sunrise }

    static let obligatory = allCases.filter(\.isObligatory)

    var title: String {
        switch self {
        case .fajr: "الفجر"
        case .sunrise: "الشروق"
        case .dhuhr: "الظهر"
        case .asr: "العصر"
        case .maghrib: "المغرب"
        case .isha: "العشاء"
        }
    }
}
