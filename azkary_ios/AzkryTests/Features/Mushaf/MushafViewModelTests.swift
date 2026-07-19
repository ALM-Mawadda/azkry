import Foundation
import Testing
@testable import Azkry

@Suite("Features/Mushaf view models")
struct MushafViewModelTests {
    @Test("Mushaf exposes every page and calculates the daily khatmah target")
    @MainActor
    func pagesAndKhatmah() throws {
        let suiteName = "AzkryTests.\(UUID().uuidString)"
        let defaults = try #require(UserDefaults(suiteName: suiteName))
        defer { defaults.removePersistentDomain(forName: suiteName) }
        let service = AssetQuranService(bundle: .main, defaults: defaults)
        let viewModel = MushafViewModel(service: service)
        let calendar = Calendar(identifier: .gregorian)
        let start = try #require(calendar.date(from: DateComponents(
            year: 2026,
            month: 7,
            day: 1,
            hour: 12
        )))

        viewModel.load(now: start)
        #expect(viewModel.surahs.count == 114)
        #expect(viewModel.juzs.count == 30)
        #expect(viewModel.pages.count == QuranKhatmah.totalPages)
        #expect(viewModel.pages.first?.surahNumber == 1)
        #expect(viewModel.pages.last?.page == QuranKhatmah.totalPages)

        viewModel.startKhatmah(totalDays: 30, now: start)
        let dayFifteen = try #require(calendar.date(byAdding: .day, value: 14, to: start))
        viewModel.load(now: dayFifteen)
        #expect(viewModel.khatmahProgress?.dayNumber == 15)
        #expect(viewModel.khatmahProgress?.targetPage == 302)
    }

    @Test("Reader opens the requested page and persists page bookmarks")
    @MainActor
    func readerPageAndBookmark() throws {
        let suiteName = "AzkryTests.\(UUID().uuidString)"
        let defaults = try #require(UserDefaults(suiteName: suiteName))
        defer { defaults.removePersistentDomain(forName: suiteName) }
        let service = AssetQuranService(bundle: .main, defaults: defaults)
        let viewModel = SurahReaderViewModel(service: service)

        viewModel.load(request: SurahOpenRequest(surahNumber: 2, startPage: 10))
        #expect(viewModel.initialPage == 10)
        #expect(try service.lastRead()?.page == 10)

        viewModel.toggleBookmark()
        #expect(viewModel.isCurrentPageBookmarked)
        #expect(try service.bookmarks().map(\.page) == [10])

        viewModel.toggleBookmark()
        #expect(!viewModel.isCurrentPageBookmarked)
        #expect(try service.bookmarks().isEmpty)
    }
}
