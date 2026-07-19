import Foundation
import SwiftData

enum BackupServiceError: LocalizedError {
    case unsupportedFormat
    case invalidData(String)

    var errorDescription: String? {
        switch self {
        case .unsupportedFormat:
            "ملف النسخة الاحتياطية غير مدعوم."
        case let .invalidData(message):
            "بيانات النسخة الاحتياطية غير صالحة: \(message)"
        }
    }
}

@MainActor
final class JSONBackupService: BackupServiceProtocol {
    private let defaults: UserDefaults
    private let decoder: JSONDecoder
    private let encoder: JSONEncoder
    private let dateKeyFormatter: DateFormatter

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
        let decoder = JSONDecoder()
        decoder.dateDecodingStrategy = .iso8601
        self.decoder = decoder
        let encoder = JSONEncoder()
        encoder.dateEncodingStrategy = .iso8601
        encoder.outputFormatting = [.prettyPrinted, .sortedKeys, .withoutEscapingSlashes]
        self.encoder = encoder
        let dateKeyFormatter = DateFormatter()
        dateKeyFormatter.calendar = Calendar(identifier: .gregorian)
        dateKeyFormatter.locale = Locale(identifier: "en_US_POSIX")
        dateKeyFormatter.timeZone = TimeZone(secondsFromGMT: 0)
        dateKeyFormatter.dateFormat = "yyyy-MM-dd"
        dateKeyFormatter.isLenient = false
        self.dateKeyFormatter = dateKeyFormatter
    }

    func exportData(in context: ModelContext) throws -> Data {
        let counts = try context.fetch(FetchDescriptor<DhikrDailyCount>()).map {
            BackupCount(dateKey: $0.dateKey, stableKey: $0.dhikrStableKey, count: $0.count)
        }
        let prayers = try context.fetch(FetchDescriptor<PrayerLog>()).map {
            BackupPrayer(dateKey: $0.dateKey, prayer: $0.prayerRawValue, completed: $0.completed)
        }
        let worship = try context.fetch(FetchDescriptor<WorshipLog>()).map {
            BackupWorship(dateKey: $0.dateKey, kind: $0.kind, completed: $0.completed)
        }
        let favorites = try context.fetch(FetchDescriptor<FavoriteDhikr>()).map {
            BackupFavorite(stableKey: $0.dhikrStableKey, createdAt: $0.createdAt)
        }
        let quranService = AssetQuranService(defaults: defaults)
        let quranLastRead = try quranService.lastRead()
        let preferences = BackupPreferences(
            appearanceMode: defaults.object(forKey: StorageKeys.appearanceMode) == nil
                ? AppearanceMode.dark.rawValue
                : defaults.integer(forKey: StorageKeys.appearanceMode),
            prayerConfiguration: DefaultsSettingsService(defaults: defaults).prayerConfiguration(),
            lastReadSurah: quranLastRead?.surahNumber ?? validLastReadSurah(),
            quranLastRead: quranLastRead,
            quranBookmarks: try quranService.bookmarks(),
            quranKhatmah: try quranService.khatmah(),
            prayerRemindersEnabled: defaults.bool(forKey: StorageKeys.prayerRemindersEnabled),
            counter: DefaultsCounterService(defaults: defaults).state(),
            fridaySunnah: fridaySunnahValues()
        )
        return try encoder.encode(BackupPayload(
            format: BackupPayload.formatIdentifier,
            version: BackupPayload.currentVersion,
            exportedAt: .now,
            counts: counts,
            prayers: prayers,
            worship: worship,
            favorites: favorites,
            preferences: preferences
        ))
    }

    func restore(from url: URL, in context: ModelContext) throws {
        let didAccess = url.startAccessingSecurityScopedResource()
        defer { if didAccess { url.stopAccessingSecurityScopedResource() } }
        let payload = try decoder.decode(BackupPayload.self, from: Data(contentsOf: url))
        guard payload.format == BackupPayload.formatIdentifier,
              payload.version == BackupPayload.currentVersion else {
            throw BackupServiceError.unsupportedFormat
        }

        let dhikrByKey = Dictionary(
            uniqueKeysWithValues: try context.fetch(FetchDescriptor<Dhikr>()).map {
                ($0.stableKey, $0.repeatCount)
            }
        )
        let quranService = AssetQuranService(defaults: defaults)
        let quranByNumber = Dictionary(
            uniqueKeysWithValues: try quranService.index().surahs.map {
                ($0.number, $0.ayahCount)
            }
        )
        try validate(payload, dhikrByKey: dhikrByKey, quranByNumber: quranByNumber)

        try context.transaction {
            for row in try context.fetch(FetchDescriptor<DhikrDailyCount>()) { context.delete(row) }
            for row in try context.fetch(FetchDescriptor<PrayerLog>()) { context.delete(row) }
            for row in try context.fetch(FetchDescriptor<WorshipLog>()) { context.delete(row) }
            for row in try context.fetch(FetchDescriptor<FavoriteDhikr>()) { context.delete(row) }

            for row in payload.counts {
                context.insert(DhikrDailyCount(
                    dateKey: row.dateKey,
                    dhikrStableKey: row.stableKey,
                    count: row.count
                ))
            }
            for row in payload.prayers {
                guard let prayer = Prayer(rawValue: row.prayer) else {
                    throw BackupServiceError.invalidData("صلاة غير معروفة")
                }
                context.insert(PrayerLog(
                    dateKey: row.dateKey,
                    prayer: prayer,
                    completed: row.completed
                ))
            }
            for row in payload.worship {
                context.insert(WorshipLog(
                    dateKey: row.dateKey,
                    kind: row.kind,
                    completed: row.completed
                ))
            }
            for row in payload.favorites {
                context.insert(FavoriteDhikr(
                    dhikrStableKey: row.stableKey,
                    createdAt: row.createdAt
                ))
            }
            try context.save()
        }
        try restorePreferences(payload.preferences)
    }

    private func validate(
        _ payload: BackupPayload,
        dhikrByKey: [String: Int],
        quranByNumber: [Int: Int]
    ) throws {
        guard payload.counts.count <= 100_000,
              payload.prayers.count <= 20_000,
              payload.worship.count <= 20_000,
              payload.favorites.count <= 2_000,
              payload.preferences.fridaySunnah.count <= 2_000 else {
            throw BackupServiceError.invalidData("الملف أكبر من الحدود الآمنة")
        }

        var countKeys = Set<String>()
        for row in payload.counts {
            let key = "\(row.dateKey)|\(row.stableKey)"
            guard validDateKey(row.dateKey), countKeys.insert(key).inserted,
                  let maximum = dhikrByKey[row.stableKey],
                  (0...maximum).contains(row.count) else {
                throw BackupServiceError.invalidData("سجل عدّ غير صالح")
            }
        }
        var prayerKeys = Set<String>()
        for row in payload.prayers {
            let key = "\(row.dateKey)|\(row.prayer)"
            guard validDateKey(row.dateKey), prayerKeys.insert(key).inserted,
                  Prayer(rawValue: row.prayer)?.isObligatory == true else {
                throw BackupServiceError.invalidData("سجل صلاة غير صالح")
            }
        }
        let worshipKinds = Set(TrackingViewModel.tasks.map(\.id))
        var worshipKeys = Set<String>()
        for row in payload.worship {
            let key = "\(row.dateKey)|\(row.kind)"
            guard validDateKey(row.dateKey), worshipKeys.insert(key).inserted,
                  worshipKinds.contains(row.kind) else {
                throw BackupServiceError.invalidData("سجل متابعة غير صالح")
            }
        }
        var favoriteKeys = Set<String>()
        for row in payload.favorites {
            guard dhikrByKey[row.stableKey] != nil,
                  favoriteKeys.insert(row.stableKey).inserted else {
                throw BackupServiceError.invalidData("مفضلة غير صالحة")
            }
        }
        try validate(payload.preferences, quranByNumber: quranByNumber)
    }

    private func validate(
        _ preferences: BackupPreferences,
        quranByNumber: [Int: Int]
    ) throws {
        let configuration = preferences.prayerConfiguration
        guard AppearanceMode(rawValue: preferences.appearanceMode) != nil,
              (-90...90).contains(configuration.location.latitude),
              (-180...180).contains(configuration.location.longitude),
              TimeZone(identifier: configuration.timeZoneIdentifier) != nil,
              preferences.lastReadSurah.map({ (1...114).contains($0) }) ?? true,
              (0...1_000_000_000).contains(preferences.counter.count),
              (0...1_000_000).contains(preferences.counter.target) else {
            throw BackupServiceError.invalidData("إعدادات غير صالحة")
        }
        if let lastRead = preferences.quranLastRead {
            guard let ayahCount = quranByNumber[lastRead.surahNumber],
                  !lastRead.surahName.isEmpty,
                  (1...ayahCount).contains(lastRead.ayahNumber),
                  (1...QuranKhatmah.totalPages).contains(lastRead.page) else {
                throw BackupServiceError.invalidData("موضع قراءة المصحف غير صالح")
            }
        }
        let bookmarks = preferences.quranBookmarks ?? []
        guard bookmarks.count <= QuranKhatmah.totalPages,
              Set(bookmarks.map(\.page)).count == bookmarks.count,
              bookmarks.allSatisfy({ bookmark in
                  quranByNumber[bookmark.surahNumber] != nil
                      && !bookmark.surahName.isEmpty
                      && (1...QuranKhatmah.totalPages).contains(bookmark.page)
              }) else {
            throw BackupServiceError.invalidData("علامات قراءة المصحف غير صالحة")
        }
        if let khatmah = preferences.quranKhatmah {
            guard validDateKey(khatmah.startDateKey),
                  (1...365).contains(khatmah.totalDays) else {
                throw BackupServiceError.invalidData("خطة الختمة غير صالحة")
            }
        }
        for (dateKey, values) in preferences.fridaySunnah {
            guard validDateKey(dateKey),
                  values.count == Set(values).count,
                  values.allSatisfy({ FridaySunnah(rawValue: $0) != nil }) else {
                throw BackupServiceError.invalidData("سجل جمعة غير صالح")
            }
        }
    }

    private func validDateKey(_ value: String) -> Bool {
        guard value.count == 10 else { return false }
        return dateKeyFormatter.date(from: value)
            .map { dateKeyFormatter.string(from: $0) == value } ?? false
    }

    private func validLastReadSurah() -> Int? {
        let value = defaults.integer(forKey: StorageKeys.lastReadSurah)
        return (1...114).contains(value) ? value : nil
    }

    private func fridaySunnahValues() -> [String: [String]] {
        var result: [String: [String]] = [:]
        for (key, value) in defaults.dictionaryRepresentation()
        where key.hasPrefix(StorageKeys.fridaySunnahPrefix) {
            let dateKey = String(key.dropFirst(StorageKeys.fridaySunnahPrefix.count))
            if let values = value as? [String] { result[dateKey] = values }
        }
        return result
    }

    private func restorePreferences(_ preferences: BackupPreferences) throws {
        defaults.set(preferences.appearanceMode, forKey: StorageKeys.appearanceMode)
        DefaultsSettingsService(defaults: defaults)
            .savePrayerConfiguration(preferences.prayerConfiguration)
        try AssetQuranService(defaults: defaults).replaceStoredState(
            lastRead: preferences.quranLastRead,
            bookmarks: preferences.quranBookmarks ?? [],
            khatmah: preferences.quranKhatmah
        )
        if preferences.quranLastRead == nil, let lastReadSurah = preferences.lastReadSurah {
            defaults.set(lastReadSurah, forKey: StorageKeys.lastReadSurah)
        }
        defaults.set(
            preferences.prayerRemindersEnabled,
            forKey: StorageKeys.prayerRemindersEnabled
        )
        DefaultsCounterService(defaults: defaults).save(preferences.counter)
        for key in defaults.dictionaryRepresentation().keys
        where key.hasPrefix(StorageKeys.fridaySunnahPrefix) {
            defaults.removeObject(forKey: key)
        }
        for (dateKey, values) in preferences.fridaySunnah {
            defaults.set(values, forKey: "\(StorageKeys.fridaySunnahPrefix)\(dateKey)")
        }
    }
}

private struct BackupPayload: Codable {
    static let formatIdentifier = "com.azkry.backup"
    static let currentVersion = 1

    let format: String
    let version: Int
    let exportedAt: Date
    let counts: [BackupCount]
    let prayers: [BackupPrayer]
    let worship: [BackupWorship]
    let favorites: [BackupFavorite]
    let preferences: BackupPreferences
}

private struct BackupCount: Codable {
    let dateKey: String
    let stableKey: String
    let count: Int
}

private struct BackupPrayer: Codable {
    let dateKey: String
    let prayer: String
    let completed: Bool
}

private struct BackupWorship: Codable {
    let dateKey: String
    let kind: String
    let completed: Bool
}

private struct BackupFavorite: Codable {
    let stableKey: String
    let createdAt: Date
}

private struct BackupPreferences: Codable {
    let appearanceMode: Int
    let prayerConfiguration: PrayerConfiguration
    let lastReadSurah: Int?
    let quranLastRead: QuranLastRead?
    let quranBookmarks: [QuranBookmark]?
    let quranKhatmah: QuranKhatmah?
    let prayerRemindersEnabled: Bool
    let counter: CounterState
    let fridaySunnah: [String: [String]]
}
