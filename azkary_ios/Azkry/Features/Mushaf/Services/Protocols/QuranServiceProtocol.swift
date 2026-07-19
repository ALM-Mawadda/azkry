import Foundation

@MainActor
protocol QuranServiceProtocol {
    func index() throws -> QuranIndex
    func surah(number: Int) throws -> QuranSurah
    func basmala() throws -> String
    func lastRead() throws -> QuranLastRead?
    func saveLastRead(_ lastRead: QuranLastRead) throws
    func bookmarks() throws -> [QuranBookmark]
    func toggleBookmark(_ bookmark: QuranBookmark) throws
    func khatmah() throws -> QuranKhatmah?
    func startKhatmah(totalDays: Int, startDateKey: String) throws
    func finishKhatmah()
    func replaceStoredState(
        lastRead: QuranLastRead?,
        bookmarks: [QuranBookmark],
        khatmah: QuranKhatmah?
    ) throws
}
