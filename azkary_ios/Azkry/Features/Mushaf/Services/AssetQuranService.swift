import Foundation

enum QuranServiceError: LocalizedError {
    case missingResource(String)
    case invalidSurahNumber(Int)
    case invalidBasmala
    case invalidKhatmah
    case invalidStoredState

    var errorDescription: String? {
        switch self {
        case let .missingResource(name):
            "تعذر العثور على بيانات المصحف: \(name)."
        case let .invalidSurahNumber(number):
            "رقم السورة غير صالح: \(number)."
        case .invalidBasmala:
            "تعذر قراءة البسملة من بيانات المصحف."
        case .invalidKhatmah:
            "مدة الختمة غير صالحة."
        case .invalidStoredState:
            "تعذر حفظ بيانات قراءة المصحف."
        }
    }
}

@MainActor
final class AssetQuranService: QuranServiceProtocol {
    private let bundle: Bundle
    private let defaults: UserDefaults
    private var cachedIndex: QuranIndex?
    private var cachedSurahs: [Int: QuranSurah] = [:]
    private let encoder = JSONEncoder()
    private let decoder = JSONDecoder()

    init(bundle: Bundle = .main, defaults: UserDefaults = .standard) {
        self.bundle = bundle
        self.defaults = defaults
    }

    func index() throws -> QuranIndex {
        if let cachedIndex { return cachedIndex }
        let decoded: QuranIndex = try decode(resource: "index")
        cachedIndex = decoded
        return decoded
    }

    func surah(number: Int) throws -> QuranSurah {
        guard (1...114).contains(number) else {
            throw QuranServiceError.invalidSurahNumber(number)
        }
        if let cached = cachedSurahs[number] { return cached }
        let decoded: QuranSurah = try decode(resource: "surah_\(number)")
        cachedSurahs[number] = decoded
        return decoded
    }

    func basmala() throws -> String {
        guard let text = try surah(number: 1).ayahs.first?.text,
              !text.isEmpty else {
            throw QuranServiceError.invalidBasmala
        }
        return text
    }

    func lastRead() throws -> QuranLastRead? {
        if let stored: QuranLastRead = try decodePreference(forKey: StorageKeys.quranLastRead) {
            return stored
        }

        let legacyNumber = defaults.integer(forKey: StorageKeys.lastReadSurah)
        guard (1...114).contains(legacyNumber),
              let summary = try index().surahs.first(where: { $0.number == legacyNumber }) else {
            return nil
        }
        let migrated = QuranLastRead(
            surahNumber: summary.number,
            surahName: summary.name,
            ayahNumber: 1,
            page: summary.page,
            updatedAt: .now
        )
        try saveLastRead(migrated)
        return migrated
    }

    func saveLastRead(_ lastRead: QuranLastRead) throws {
        guard (1...114).contains(lastRead.surahNumber),
              (1...QuranKhatmah.totalPages).contains(lastRead.page),
              lastRead.ayahNumber > 0 else {
            throw QuranServiceError.invalidStoredState
        }
        try encodePreference(lastRead, forKey: StorageKeys.quranLastRead)
        defaults.set(lastRead.surahNumber, forKey: StorageKeys.lastReadSurah)
    }

    func bookmarks() throws -> [QuranBookmark] {
        let stored: [QuranBookmark]? = try decodePreference(forKey: StorageKeys.quranBookmarks)
        return stored ?? []
    }

    func toggleBookmark(_ bookmark: QuranBookmark) throws {
        guard (1...114).contains(bookmark.surahNumber),
              (1...QuranKhatmah.totalPages).contains(bookmark.page) else {
            throw QuranServiceError.invalidStoredState
        }
        let current = try bookmarks()
        let updated = if current.contains(where: { $0.page == bookmark.page }) {
            current.filter { $0.page != bookmark.page }
        } else {
            (current + [bookmark]).sorted { $0.page < $1.page }
        }
        try encodePreference(updated, forKey: StorageKeys.quranBookmarks)
    }

    func khatmah() throws -> QuranKhatmah? {
        try decodePreference(forKey: StorageKeys.quranKhatmah)
    }

    func startKhatmah(totalDays: Int, startDateKey: String) throws {
        guard (1...365).contains(totalDays), startDateKey.count == 10 else {
            throw QuranServiceError.invalidKhatmah
        }
        try encodePreference(
            QuranKhatmah(startDateKey: startDateKey, totalDays: totalDays),
            forKey: StorageKeys.quranKhatmah
        )
    }

    func finishKhatmah() {
        defaults.removeObject(forKey: StorageKeys.quranKhatmah)
    }

    func replaceStoredState(
        lastRead: QuranLastRead?,
        bookmarks: [QuranBookmark],
        khatmah: QuranKhatmah?
    ) throws {
        if let lastRead {
            try saveLastRead(lastRead)
        } else {
            defaults.removeObject(forKey: StorageKeys.quranLastRead)
            defaults.removeObject(forKey: StorageKeys.lastReadSurah)
        }
        try encodePreference(bookmarks, forKey: StorageKeys.quranBookmarks)
        if let khatmah {
            try encodePreference(khatmah, forKey: StorageKeys.quranKhatmah)
        } else {
            defaults.removeObject(forKey: StorageKeys.quranKhatmah)
        }
    }

    private func decode<Value: Decodable>(resource: String) throws -> Value {
        guard let url = bundle.url(forResource: resource, withExtension: "json") else {
            throw QuranServiceError.missingResource(resource)
        }
        return try JSONDecoder().decode(Value.self, from: Data(contentsOf: url))
    }

    private func decodePreference<Value: Decodable>(forKey key: String) throws -> Value? {
        guard let data = defaults.data(forKey: key) else { return nil }
        do {
            return try decoder.decode(Value.self, from: data)
        } catch {
            throw QuranServiceError.invalidStoredState
        }
    }

    private func encodePreference<Value: Encodable>(_ value: Value, forKey key: String) throws {
        do {
            defaults.set(try encoder.encode(value), forKey: key)
        } catch {
            throw QuranServiceError.invalidStoredState
        }
    }
}
