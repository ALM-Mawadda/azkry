import Foundation
import SwiftData
import Testing
@testable import Azkry

@Suite("Features/Settings backup")
struct BackupServiceTests {
    @Test("Backup round-trip restores progress and preferences")
    @MainActor
    func roundTrip() throws {
        let suiteName = "AzkryTests.\(UUID().uuidString)"
        let defaults = UserDefaults(suiteName: suiteName)!
        defer { defaults.removePersistentDomain(forName: suiteName) }
        let container = try AzkrySchema.makeModelContainer(inMemory: true)
        let context = ModelContext(container)
        try SwiftDataAdhkarLibraryService(bundle: .main, defaults: defaults)
            .prepareLibrary(in: context)

        context.insert(DhikrDailyCount(
            dateKey: "2026-07-19",
            dhikrStableKey: "morning/item_1",
            count: 1
        ))
        context.insert(FavoriteDhikr(dhikrStableKey: "morning/item_1"))
        context.insert(PrayerLog(dateKey: "2026-07-19", prayer: .fajr, completed: true))
        context.insert(WorshipLog(
            dateKey: "2026-07-19",
            kind: "quran",
            completed: true
        ))
        try context.save()
        defaults.set(2, forKey: StorageKeys.appearanceMode)
        let quranService = AssetQuranService(defaults: defaults)
        try quranService.saveLastRead(QuranLastRead(
            surahNumber: 18,
            surahName: "سُورَةُ الكَهۡفِ",
            ayahNumber: 10,
            page: 294,
            updatedAt: .now
        ))
        try quranService.toggleBookmark(QuranBookmark(
            surahNumber: 18,
            surahName: "سُورَةُ الكَهۡفِ",
            page: 294
        ))
        try quranService.startKhatmah(totalDays: 30, startDateKey: "2026-07-19")
        DefaultsCounterService(defaults: defaults).save(CounterState(count: 12, target: 0))
        defaults.set(["ghusl", "kahf"], forKey: "\(StorageKeys.fridaySunnahPrefix)2026-07-18")

        let service = JSONBackupService(defaults: defaults)
        let url = FileManager.default.temporaryDirectory
            .appending(path: "azkry-backup-\(UUID().uuidString).json")
        defer { try? FileManager.default.removeItem(at: url) }
        try service.exportData(in: context).write(to: url, options: .atomic)

        for row in try context.fetch(FetchDescriptor<DhikrDailyCount>()) { context.delete(row) }
        for row in try context.fetch(FetchDescriptor<FavoriteDhikr>()) { context.delete(row) }
        for row in try context.fetch(FetchDescriptor<PrayerLog>()) { context.delete(row) }
        for row in try context.fetch(FetchDescriptor<WorshipLog>()) { context.delete(row) }
        try context.save()
        defaults.set(0, forKey: StorageKeys.appearanceMode)
        try quranService.replaceStoredState(lastRead: nil, bookmarks: [], khatmah: nil)
        DefaultsCounterService(defaults: defaults).save(CounterState(count: 0, target: 33))

        try service.restore(from: url, in: context)

        #expect(try context.fetch(FetchDescriptor<DhikrDailyCount>()).first?.count == 1)
        #expect(try context.fetchCount(FetchDescriptor<FavoriteDhikr>()) == 1)
        #expect(try context.fetch(FetchDescriptor<PrayerLog>()).first?.completed == true)
        #expect(try context.fetch(FetchDescriptor<WorshipLog>()).first?.kind == "quran")
        #expect(defaults.integer(forKey: StorageKeys.appearanceMode) == 2)
        #expect(defaults.integer(forKey: StorageKeys.lastReadSurah) == 18)
        #expect(try quranService.lastRead()?.ayahNumber == 10)
        #expect(try quranService.bookmarks().map(\.page) == [294])
        #expect(try quranService.khatmah()?.totalDays == 30)
        #expect(DefaultsCounterService(defaults: defaults).state() == CounterState(count: 12, target: 0))
        #expect(
            Set(defaults.stringArray(forKey: "\(StorageKeys.fridaySunnahPrefix)2026-07-18") ?? [])
                == Set(["ghusl", "kahf"])
        )
    }

    @Test("Invalid backup is rejected before stored progress changes")
    @MainActor
    func invalidBackupIsAtomic() throws {
        let suiteName = "AzkryTests.\(UUID().uuidString)"
        let defaults = UserDefaults(suiteName: suiteName)!
        defer { defaults.removePersistentDomain(forName: suiteName) }
        let container = try AzkrySchema.makeModelContainer(inMemory: true)
        let context = ModelContext(container)
        try SwiftDataAdhkarLibraryService(bundle: .main, defaults: defaults)
            .prepareLibrary(in: context)
        context.insert(DhikrDailyCount(
            dateKey: "2026-07-19",
            dhikrStableKey: "morning/item_1",
            count: 1
        ))
        try context.save()

        let service = JSONBackupService(defaults: defaults)
        var json = try #require(
            JSONSerialization.jsonObject(with: service.exportData(in: context))
                as? [String: Any]
        )
        var counts = try #require(json["counts"] as? [[String: Any]])
        counts.append(try #require(counts.first))
        json["counts"] = counts
        let url = FileManager.default.temporaryDirectory
            .appending(path: "azkry-invalid-\(UUID().uuidString).json")
        defer { try? FileManager.default.removeItem(at: url) }
        try JSONSerialization.data(withJSONObject: json).write(to: url, options: .atomic)

        #expect(throws: BackupServiceError.self) {
            try service.restore(from: url, in: context)
        }
        #expect(try context.fetch(FetchDescriptor<DhikrDailyCount>()).first?.count == 1)
    }
}
