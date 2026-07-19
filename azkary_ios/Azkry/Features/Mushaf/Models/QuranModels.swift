import Foundation

struct QuranIndex: Decodable, Sendable {
    let surahs: [SurahSummary]
    let juzs: [JuzSummary]
}

struct SurahSummary: Decodable, Identifiable, Hashable, Sendable {
    let number: Int
    let name: String
    let englishName: String
    let revelationType: String
    let ayahCount: Int
    let page: Int
    let juz: Int

    var id: Int { number }

    private enum CodingKeys: String, CodingKey {
        case number = "n"
        case name
        case englishName = "en"
        case revelationType = "type"
        case ayahCount = "count"
        case page
        case juz
    }
}

struct JuzSummary: Decodable, Identifiable, Hashable, Sendable {
    let number: Int
    let surahNumber: Int
    let ayahNumber: Int
    let page: Int

    var id: Int { number }

    private enum CodingKeys: String, CodingKey {
        case number = "n"
        case surahNumber = "surah"
        case ayahNumber = "ayah"
        case page
    }
}

struct QuranSurah: Decodable, Sendable {
    let number: Int
    let name: String
    let ayahs: [QuranAyah]

    private enum CodingKeys: String, CodingKey {
        case number = "n"
        case name
        case ayahs
    }
}

struct QuranAyah: Decodable, Identifiable, Hashable, Sendable {
    let number: Int
    let text: String
    let page: Int
    let juz: Int

    var id: Int { number }

    private enum CodingKeys: String, CodingKey {
        case number = "n"
        case text = "t"
        case page = "p"
        case juz = "j"
    }
}

struct SurahOpenRequest: Hashable, Sendable {
    let surahNumber: Int
    let startAyah: Int?
    let startPage: Int?

    init(surahNumber: Int, startAyah: Int? = nil, startPage: Int? = nil) {
        self.surahNumber = surahNumber
        self.startAyah = startAyah
        self.startPage = startPage
    }
}

struct QuranLastRead: Codable, Equatable, Sendable {
    let surahNumber: Int
    let surahName: String
    let ayahNumber: Int
    let page: Int
    let updatedAt: Date
}

struct QuranBookmark: Codable, Identifiable, Hashable, Sendable {
    let surahNumber: Int
    let surahName: String
    let page: Int

    var id: Int { page }
}

struct QuranKhatmah: Codable, Equatable, Sendable {
    static let totalPages = 604

    let startDateKey: String
    let totalDays: Int
}

struct MushafPageEntry: Identifiable, Hashable, Sendable {
    let page: Int
    let surahNumber: Int
    let surahName: String

    var id: Int { page }
}

struct QuranReaderPage: Identifiable, Hashable, Sendable {
    let page: Int
    let juz: Int
    let ayahs: [QuranAyah]

    var id: Int { page }
}

struct KhatmahProgress: Equatable, Sendable {
    let dayNumber: Int
    let totalDays: Int
    let targetPage: Int
    let currentPage: Int

    var fraction: Double {
        min(1, max(0, Double(currentPage) / Double(QuranKhatmah.totalPages)))
    }
}
