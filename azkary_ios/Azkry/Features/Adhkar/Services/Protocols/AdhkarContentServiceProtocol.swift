import SwiftData

@MainActor
protocol AdhkarContentServiceProtocol {
    func categories(in context: ModelContext, includeExclusive: Bool) throws -> [DhikrCategory]
    func category(key: String, in context: ModelContext) throws -> DhikrCategory?
    func allDhikr(in context: ModelContext) throws -> [Dhikr]
    func favoriteKeys(in context: ModelContext) throws -> Set<String>
    func counts(dateKey: String, in context: ModelContext) throws -> [String: Int]
    func increment(_ dhikr: Dhikr, dateKey: String, in context: ModelContext) throws -> Int
    func toggleFavorite(stableKey: String, in context: ModelContext) throws -> Bool
}
