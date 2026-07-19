import Foundation

enum MainDestination: Hashable {
    case adhkar
    case dhikrCategory(String)
    case tracking
    case mushaf
    case surah(SurahOpenRequest)
    case pages
    case page(PageKey)
    case settings
    case search
    case prayerTimes
    case hijriCalendar
    case friday
    case qibla
    case counter
    case favorites
    case exclusive

    var title: String {
        switch self {
        case .adhkar: "الأذكار والأدعية"
        case .dhikrCategory: "الأذكار"
        case .tracking: "متابعتي"
        case .mushaf: "المصحف"
        case .surah: "المصحف"
        case .pages: "صفحات"
        case let .page(key): key.title
        case .settings: "الإعدادات"
        case .search: "البحث"
        case .prayerTimes: "مواقيت الصلاة"
        case .hijriCalendar: "التقويم الهجري"
        case .friday: "يوم الجمعة"
        case .qibla: "القبلة"
        case .counter: "العداد"
        case .favorites: "المفضلة"
        case .exclusive: "حصريات"
        }
    }

    var systemImage: String {
        switch self {
        case .adhkar: "sparkles"
        case .dhikrCategory: "text.book.closed.fill"
        case .tracking: "chart.bar.fill"
        case .mushaf: "book.closed.fill"
        case .surah: "book.pages.fill"
        case .pages: "square.grid.2x2.fill"
        case let .page(key): key.systemImage
        case .settings: "gearshape.fill"
        case .search: "magnifyingglass"
        case .prayerTimes: "clock.fill"
        case .hijriCalendar: "calendar"
        case .friday: "sun.max.fill"
        case .qibla: "location.north.circle.fill"
        case .counter: "circle.dotted.circle.fill"
        case .favorites: "heart.fill"
        case .exclusive: "seal.fill"
        }
    }
}
