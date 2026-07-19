import Foundation
import Testing
@testable import Azkry

@Suite("Features/AssetQuranService")
struct AssetQuranServiceTests {
    @Test("Bundled index and surah assets form the complete Quran")
    @MainActor
    func completeQuran() throws {
        let suiteName = "AzkryTests.\(UUID().uuidString)"
        let defaults = try #require(UserDefaults(suiteName: suiteName))
        defer { defaults.removePersistentDomain(forName: suiteName) }
        let service = AssetQuranService(bundle: .main, defaults: defaults)
        let index = try service.index()

        #expect(index.surahs.count == 114)
        #expect(index.juzs.count == 30)
        #expect(index.surahs.reduce(0, { $0 + $1.ayahCount }) == 6_236)

        let fatiha = try service.surah(number: 1)
        #expect(fatiha.ayahs.count == 7)
        #expect(fatiha.ayahs.first?.number == 1)

        let lastRead = QuranLastRead(
            surahNumber: 18,
            surahName: "سُورَةُ الكَهۡفِ",
            ayahNumber: 10,
            page: 294,
            updatedAt: .now
        )
        try service.saveLastRead(lastRead)
        #expect(try service.lastRead() == lastRead)

        let bookmark = QuranBookmark(
            surahNumber: 18,
            surahName: "سُورَةُ الكَهۡفِ",
            page: 294
        )
        try service.toggleBookmark(bookmark)
        #expect(try service.bookmarks() == [bookmark])
        try service.toggleBookmark(bookmark)
        #expect(try service.bookmarks().isEmpty)

        try service.startKhatmah(totalDays: 30, startDateKey: "2026-07-19")
        #expect(
            try service.khatmah()
                == QuranKhatmah(startDateKey: "2026-07-19", totalDays: 30)
        )
        service.finishKhatmah()
        #expect(try service.khatmah() == nil)
    }
}
