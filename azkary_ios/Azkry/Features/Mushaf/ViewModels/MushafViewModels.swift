import Foundation
import Observation

@MainActor
@Observable
final class MushafViewModel {
    private(set) var surahs: [SurahSummary] = []
    private(set) var juzs: [JuzSummary] = []
    private(set) var pages: [MushafPageEntry] = []
    private(set) var lastRead: QuranLastRead?
    private(set) var bookmarks: [QuranBookmark] = []
    private(set) var khatmahProgress: KhatmahProgress?
    private(set) var errorMessage: String?
    private let service: any QuranServiceProtocol

    init(service: (any QuranServiceProtocol)? = nil) {
        self.service = service ?? AssetQuranService()
    }

    func load(now: Date = .now) {
        do {
            let index = try service.index()
            surahs = index.surahs
            juzs = index.juzs
            pages = (1...QuranKhatmah.totalPages).compactMap { page in
                guard let surah = index.surahs.last(where: { $0.page <= page }) else {
                    return nil
                }
                return MushafPageEntry(
                    page: page,
                    surahNumber: surah.number,
                    surahName: surah.name
                )
            }
            try loadStoredState(now: now)
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func startKhatmah(totalDays: Int, now: Date = .now) {
        do {
            try service.startKhatmah(
                totalDays: totalDays,
                startDateKey: LocalDateKey.make(from: now)
            )
            try loadStoredState(now: now)
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func finishKhatmah() {
        service.finishKhatmah()
        khatmahProgress = nil
    }

    private func loadStoredState(now: Date) throws {
        lastRead = try service.lastRead()
        bookmarks = try service.bookmarks()
        khatmahProgress = try service.khatmah().map {
            progress(for: $0, lastRead: lastRead, now: now)
        }
    }

    private func progress(
        for khatmah: QuranKhatmah,
        lastRead: QuranLastRead?,
        now: Date
    ) -> KhatmahProgress {
        let formatter = DateFormatter()
        formatter.calendar = Calendar(identifier: .gregorian)
        formatter.locale = Locale(identifier: "en_US_POSIX")
        formatter.timeZone = .autoupdatingCurrent
        formatter.dateFormat = "yyyy-MM-dd"
        let start = formatter.date(from: khatmah.startDateKey) ?? now
        let calendar = formatter.calendar ?? Calendar(identifier: .gregorian)
        let elapsed = calendar.dateComponents(
            [.day],
            from: calendar.startOfDay(for: start),
            to: calendar.startOfDay(for: now)
        ).day ?? 0
        let dayNumber = min(khatmah.totalDays, max(1, elapsed + 1))
        let targetPage = min(
            QuranKhatmah.totalPages,
            Int(ceil(Double(QuranKhatmah.totalPages * dayNumber) / Double(khatmah.totalDays)))
        )
        return KhatmahProgress(
            dayNumber: dayNumber,
            totalDays: khatmah.totalDays,
            targetPage: targetPage,
            currentPage: lastRead?.page ?? 1
        )
    }
}

@MainActor
@Observable
final class SurahReaderViewModel {
    private(set) var surah: QuranSurah?
    private(set) var pages: [QuranReaderPage] = []
    private(set) var basmala: String?
    private(set) var initialPage: Int?
    private(set) var currentPage: Int?
    private(set) var bookmarkedPages = Set<Int>()
    private(set) var errorMessage: String?
    private let service: any QuranServiceProtocol

    init(service: (any QuranServiceProtocol)? = nil) {
        self.service = service ?? AssetQuranService()
    }

    var isCurrentPageBookmarked: Bool {
        currentPage.map(bookmarkedPages.contains) ?? false
    }

    func load(request: SurahOpenRequest) {
        do {
            let content = try service.surah(number: request.surahNumber)
            let grouped = Dictionary(grouping: content.ayahs, by: \.page)
            let readerPages = grouped.keys.sorted().compactMap { page -> QuranReaderPage? in
                guard let ayahs = grouped[page], let first = ayahs.first else { return nil }
                return QuranReaderPage(page: page, juz: first.juz, ayahs: ayahs)
            }
            let requestedPage = if let startPage = request.startPage {
                readerPages.first(where: { $0.page >= startPage })?.page
            } else if let startAyah = request.startAyah {
                readerPages.first(where: { page in
                    page.ayahs.contains(where: { $0.number >= startAyah })
                })?.page
            } else {
                readerPages.first?.page
            }

            surah = content
            pages = readerPages
            basmala = request.surahNumber == 1 || request.surahNumber == 9
                ? nil
                : try service.basmala()
            initialPage = requestedPage ?? readerPages.first?.page
            currentPage = initialPage
            bookmarkedPages = Set(try service.bookmarks().map(\.page))
            if let page = readerPages.first(where: { $0.page == currentPage }) {
                try saveLastRead(content: content, page: page)
            }
            errorMessage = nil
        } catch {
            basmala = nil
            errorMessage = error.localizedDescription
        }
    }

    func pageBecameVisible(_ pageNumber: Int) {
        guard pageNumber != currentPage,
              let content = surah,
              let page = pages.first(where: { $0.page == pageNumber }) else {
            return
        }
        do {
            try saveLastRead(content: content, page: page)
            currentPage = pageNumber
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func toggleBookmark() {
        guard let content = surah, let page = currentPage else { return }
        do {
            try service.toggleBookmark(QuranBookmark(
                surahNumber: content.number,
                surahName: content.name,
                page: page
            ))
            bookmarkedPages = Set(try service.bookmarks().map(\.page))
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    private func saveLastRead(content: QuranSurah, page: QuranReaderPage) throws {
        try service.saveLastRead(QuranLastRead(
            surahNumber: content.number,
            surahName: content.name,
            ayahNumber: page.ayahs.first?.number ?? 1,
            page: page.page,
            updatedAt: .now
        ))
    }
}
