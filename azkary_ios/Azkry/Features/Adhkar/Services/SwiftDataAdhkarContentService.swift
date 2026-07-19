import Foundation
import SwiftData

@MainActor
final class SwiftDataAdhkarContentService: AdhkarContentServiceProtocol {
    func categories(
        in context: ModelContext,
        includeExclusive: Bool = false
    ) throws -> [DhikrCategory] {
        let rows = try context.fetch(
            FetchDescriptor<DhikrCategory>(sortBy: [SortDescriptor(\.sortOrder)])
        )
        return includeExclusive ? rows : rows.filter { !$0.key.hasPrefix("x_") }
    }

    func category(key: String, in context: ModelContext) throws -> DhikrCategory? {
        try context.fetch(FetchDescriptor<DhikrCategory>()).first { $0.key == key }
    }

    func allDhikr(in context: ModelContext) throws -> [Dhikr] {
        try context.fetch(FetchDescriptor<Dhikr>())
            .sorted {
                if $0.category?.sortOrder == $1.category?.sortOrder {
                    return $0.sortOrder < $1.sortOrder
                }
                return ($0.category?.sortOrder ?? 0) < ($1.category?.sortOrder ?? 0)
            }
    }

    func favoriteKeys(in context: ModelContext) throws -> Set<String> {
        Set(try context.fetch(FetchDescriptor<FavoriteDhikr>()).map(\.dhikrStableKey))
    }

    func counts(dateKey: String, in context: ModelContext) throws -> [String: Int] {
        Dictionary(
            uniqueKeysWithValues: try context.fetch(FetchDescriptor<DhikrDailyCount>())
                .filter { $0.dateKey == dateKey }
                .map { ($0.dhikrStableKey, $0.count) }
        )
    }

    func increment(
        _ dhikr: Dhikr,
        dateKey: String,
        in context: ModelContext
    ) throws -> Int {
        let recordKey = "\(dateKey)|\(dhikr.stableKey)"
        let existing = try context.fetch(FetchDescriptor<DhikrDailyCount>())
            .first { $0.recordKey == recordKey }
        let next = min((existing?.count ?? 0) + 1, dhikr.repeatCount)
        if let existing {
            existing.count = next
        } else {
            context.insert(
                DhikrDailyCount(
                    dateKey: dateKey,
                    dhikrStableKey: dhikr.stableKey,
                    count: next
                )
            )
        }
        try context.save()
        return next
    }

    func toggleFavorite(stableKey: String, in context: ModelContext) throws -> Bool {
        if let existing = try context.fetch(FetchDescriptor<FavoriteDhikr>())
            .first(where: { $0.dhikrStableKey == stableKey }) {
            context.delete(existing)
            try context.save()
            return false
        }
        context.insert(FavoriteDhikr(dhikrStableKey: stableKey))
        try context.save()
        return true
    }
}
