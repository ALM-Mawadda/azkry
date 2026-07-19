import Foundation

enum PageKey: String, CaseIterable, Hashable, Identifiable, Sendable {
    case names
    case friday
    case rawatib
    case duha
    case duaEtiquette
    case dhikrAndDua
    case forbiddenTimes
    case ramadanQada
    case deceasedDuas
    case ayahTafsir

    var id: String { rawValue }

    var title: String {
        switch self {
        case .names: "أسماء الله الحسنى"
        case .friday: "الجمعة"
        case .rawatib: "السنن الرواتب"
        case .duha: "صلاة الضحى"
        case .duaEtiquette: "آداب الدعاء"
        case .dhikrAndDua: "الدعاء والذِّكر"
        case .forbiddenTimes: "أوقات النهي"
        case .ramadanQada: "قضاء رمضان"
        case .deceasedDuas: "أدعية الميت"
        case .ayahTafsir: "آية وتفسير"
        }
    }

    var systemImage: String {
        switch self {
        case .names: "text.book.closed.fill"
        case .friday: "calendar"
        case .rawatib: "moon.stars.fill"
        case .duha: "sun.max.fill"
        case .duaEtiquette: "book.pages.fill"
        case .dhikrAndDua: "sparkles"
        case .forbiddenTimes: "clock.badge.xmark"
        case .ramadanQada: "arrow.trianglehead.2.clockwise"
        case .deceasedDuas: "leaf.fill"
        case .ayahTafsir: "text.magnifyingglass"
        }
    }
}

struct PageSection: Identifiable, Hashable, Sendable {
    let heading: String?
    let body: String
    let source: String?
    let note: String?

    var id: String { [heading, body, source].compactMap(\.self).joined(separator: "|") }
}

struct AzkryPage: Identifiable, Hashable, Sendable {
    let key: PageKey
    let sections: [PageSection]
    let names: [String]

    var id: PageKey { key }
    var title: String { key.title }
}
