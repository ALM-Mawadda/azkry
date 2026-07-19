import Foundation
import SwiftData
import Testing
@testable import Azkry

@Suite("Features/Adhkar seed")
struct AdhkarSeedTests {
    @Test("Bundled library matches Android's 18 categories and 374 items exactly once")
    @MainActor
    func bundledLibrarySeedsIdempotently() throws {
        let suiteName = "AzkryTests.\(UUID().uuidString)"
        let defaults = UserDefaults(suiteName: suiteName)!
        defer { defaults.removePersistentDomain(forName: suiteName) }

        let container = try AzkrySchema.makeModelContainer(inMemory: true)
        let context = ModelContext(container)
        let service = SwiftDataAdhkarLibraryService(
            bundle: .main,
            defaults: defaults
        )

        try service.prepareLibrary(in: context)

        #expect(try context.fetchCount(FetchDescriptor<DhikrCategory>()) == 18)
        #expect(try context.fetchCount(FetchDescriptor<Dhikr>()) == 374)

        let morning = try #require(
            try context.fetch(
                FetchDescriptor<DhikrCategory>(
                    predicate: #Predicate { $0.key == "morning" }
                )
            ).first
        )
        let firstMorning = try #require(morning.items.min(by: { $0.sortOrder < $1.sortOrder }))
        #expect(firstMorning.stableKey == "morning/item_1")

        try service.prepareLibrary(in: context)

        #expect(try context.fetchCount(FetchDescriptor<DhikrCategory>()) == 18)
        #expect(try context.fetchCount(FetchDescriptor<Dhikr>()) == 374)
    }

    @Test("Invalid replacement is rejected before existing progress is changed")
    @MainActor
    func invalidReplacementPreservesLibrary() throws {
        let suiteName = "AzkryTests.\(UUID().uuidString)"
        let defaults = UserDefaults(suiteName: suiteName)!
        defer { defaults.removePersistentDomain(forName: suiteName) }
        let container = try AzkrySchema.makeModelContainer(inMemory: true)
        let context = ModelContext(container)
        let validService = SwiftDataAdhkarLibraryService(bundle: .main, defaults: defaults)
        try validService.prepareLibrary(in: context)

        let favorite = FavoriteDhikr(dhikrStableKey: "morning/item_1")
        let count = DhikrDailyCount(
            dateKey: "2026-07-19",
            dhikrStableKey: "morning/item_1",
            count: 1
        )
        context.insert(favorite)
        context.insert(count)
        try context.save()
        defaults.set(0, forKey: StorageKeys.adhkarContentRevision)

        let invalidJSON = Data(
            """
            {"categories":[{"key":"broken","title":"غير صالح","iconKey":"x","items":[{"text":"","count":0}]}]}
            """.utf8
        )
        let invalidService = SwiftDataAdhkarLibraryService(
            defaults: defaults,
            seedDataOverride: [invalidJSON]
        )

        do {
            try invalidService.prepareLibrary(in: context)
            Issue.record("Expected invalid seed replacement to fail")
        } catch {
            #expect(error is AdhkarLibraryError)
        }

        #expect(try context.fetchCount(FetchDescriptor<DhikrCategory>()) == 18)
        #expect(try context.fetchCount(FetchDescriptor<Dhikr>()) == 374)
        #expect(try context.fetchCount(FetchDescriptor<FavoriteDhikr>()) == 1)
        #expect(try context.fetchCount(FetchDescriptor<DhikrDailyCount>()) == 1)
    }

    @Test("Valid replacement preserves stable progress and caps it to the new repeat count")
    @MainActor
    func validReplacementPreservesStableProgress() throws {
        let suiteName = "AzkryTests.\(UUID().uuidString)"
        let defaults = UserDefaults(suiteName: suiteName)!
        defer { defaults.removePersistentDomain(forName: suiteName) }
        let container = try AzkrySchema.makeModelContainer(inMemory: true)
        let context = ModelContext(container)
        let bundledService = SwiftDataAdhkarLibraryService(bundle: .main, defaults: defaults)
        try bundledService.prepareLibrary(in: context)

        context.insert(FavoriteDhikr(dhikrStableKey: "morning/item_1"))
        context.insert(DhikrDailyCount(
            dateKey: "2026-07-19",
            dhikrStableKey: "morning/item_1",
            count: 8
        ))
        try context.save()
        defaults.set(0, forKey: StorageKeys.adhkarContentRevision)

        let replacement = Data(
            """
            {"categories":[{"key":"morning","title":"أذكار الصباح","iconKey":"sun.max.fill","items":[{"text":"نص مختبر","count":3}]}]}
            """.utf8
        )
        let replacementService = SwiftDataAdhkarLibraryService(
            defaults: defaults,
            seedDataOverride: [replacement]
        )
        try replacementService.prepareLibrary(in: context)

        let favorite = try context.fetch(FetchDescriptor<FavoriteDhikr>())
        let counts = try context.fetch(FetchDescriptor<DhikrDailyCount>())
        #expect(favorite.map(\.dhikrStableKey) == ["morning/item_1"])
        #expect(counts.count == 1)
        #expect(counts.first?.count == 3)
    }
}
