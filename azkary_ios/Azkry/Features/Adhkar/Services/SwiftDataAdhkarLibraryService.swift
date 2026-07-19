import Foundation
import SwiftData

enum AdhkarLibraryError: LocalizedError {
    case missingSeed(String)
    case duplicateCategoryKey(String)
    case duplicateStableKey(String)
    case invalidCategoryKey(String)
    case invalidItemKey(category: String, index: Int)
    case emptyText(category: String, index: Int)
    case invalidRepeatCount(category: String, index: Int)

    var errorDescription: String? {
        switch self {
        case .missingSeed(let resource):
            "تعذر العثور على مكتبة الأذكار المرفقة بالتطبيق: \(resource)."
        case .duplicateCategoryKey(let key):
            "مفتاح قسم الأذكار مكرر: \(key)."
        case .duplicateStableKey(let key):
            "مفتاح الذكر مكرر: \(key)."
        case .invalidCategoryKey(let key):
            "مفتاح قسم الأذكار غير صالح: \(key)."
        case .invalidItemKey(let category, let index):
            "مفتاح الذكر غير صالح في \(category) عند العنصر \(index + 1)."
        case .emptyText(let category, let index):
            "نص الذكر فارغ في \(category) عند العنصر \(index + 1)."
        case .invalidRepeatCount(let category, let index):
            "عدد التكرار غير صالح في \(category) عند العنصر \(index + 1)."
        }
    }
}

@MainActor
final class SwiftDataAdhkarLibraryService: AdhkarLibraryServiceProtocol {
    /// Bump only when the canonical bundled dataset changes. Stable-keyed
    /// favorites and daily progress survive replacement while removed items
    /// are cleaned up and retained counts are capped to the new repeat value.
    static let contentRevision = 2

    private let bundle: Bundle
    private let defaults: UserDefaults
    private let seedDataOverride: [Data]?
    private let seedResources = ["athkar_seed", "azkry_extra_seed"]

    init(
        bundle: Bundle = .main,
        defaults: UserDefaults = .standard,
        seedDataOverride: [Data]? = nil
    ) {
        self.bundle = bundle
        self.defaults = defaults
        self.seedDataOverride = seedDataOverride
    }

    func prepareLibrary(in context: ModelContext) throws {
        let storedRevision = defaults.integer(forKey: StorageKeys.adhkarContentRevision)
        let existing = try context.fetchCount(FetchDescriptor<DhikrCategory>())
        guard storedRevision != Self.contentRevision || existing == 0 else { return }

        let categories = try loadCategories()
        try validate(categories)
        try replaceLibrary(with: categories, in: context)
        defaults.set(Self.contentRevision, forKey: StorageKeys.adhkarContentRevision)
    }

    func categories(in context: ModelContext) throws -> [DhikrCategory] {
        try context.fetch(
            FetchDescriptor<DhikrCategory>(
                sortBy: [SortDescriptor(\.sortOrder)]
            )
        )
    }

    private func loadCategories() throws -> [SeedCategory] {
        let dataDocuments: [Data]
        if let seedDataOverride {
            dataDocuments = seedDataOverride
        } else {
            dataDocuments = try seedResources.map { resource in
                guard let url = bundle.url(forResource: resource, withExtension: "json") else {
                    throw AdhkarLibraryError.missingSeed(resource)
                }
                return try Data(contentsOf: url)
            }
        }
        return try dataDocuments.flatMap {
            try JSONDecoder().decode(SeedDocument.self, from: $0).categories
        }
    }

    private func validate(_ categories: [SeedCategory]) throws {
        var categoryKeys = Set<String>()
        var stableKeys = Set<String>()
        for category in categories {
            guard !category.key.isEmpty, !category.key.contains("/") else {
                throw AdhkarLibraryError.invalidCategoryKey(category.key)
            }
            guard categoryKeys.insert(category.key).inserted else {
                throw AdhkarLibraryError.duplicateCategoryKey(category.key)
            }
            for (index, item) in category.items.enumerated() {
                guard !item.text.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty else {
                    throw AdhkarLibraryError.emptyText(category: category.key, index: index)
                }
                guard item.count > 0 else {
                    throw AdhkarLibraryError.invalidRepeatCount(category: category.key, index: index)
                }
                let stableKey = try stableDhikrKey(
                    categoryKey: category.key,
                    itemKey: item.key,
                    index: index
                )
                guard stableKeys.insert(stableKey).inserted else {
                    throw AdhkarLibraryError.duplicateStableKey(stableKey)
                }
            }
        }
    }

    private func replaceLibrary(
        with categories: [SeedCategory],
        in context: ModelContext
    ) throws {
        try context.transaction {
            let existingCategories = try context.fetch(FetchDescriptor<DhikrCategory>())
            let existingItems = try context.fetch(FetchDescriptor<Dhikr>())
            var categoriesByKey = Dictionary(
                uniqueKeysWithValues: existingCategories.map { ($0.key, $0) }
            )
            var itemsByKey = Dictionary(
                uniqueKeysWithValues: existingItems.map { ($0.stableKey, $0) }
            )
            var retainedCategoryKeys = Set<String>()
            var retainedItemKeys = Set<String>()
            var retainedRepeatCounts: [String: Int] = [:]

            for (categoryIndex, seedCategory) in categories.enumerated() {
                let category = categoriesByKey.removeValue(forKey: seedCategory.key)
                    ?? DhikrCategory(
                        key: seedCategory.key,
                        title: seedCategory.title,
                        iconKey: seedCategory.iconKey,
                        sortOrder: categoryIndex
                    )
                if category.modelContext == nil { context.insert(category) }
                category.title = seedCategory.title
                category.iconKey = seedCategory.iconKey
                category.sortOrder = categoryIndex
                retainedCategoryKeys.insert(seedCategory.key)

                var seededItems: [Dhikr] = []
                for (itemIndex, seedItem) in seedCategory.items.enumerated() {
                    let stableKey = try stableDhikrKey(
                        categoryKey: seedCategory.key,
                        itemKey: seedItem.key,
                        index: itemIndex
                    )
                    let item = itemsByKey.removeValue(forKey: stableKey)
                        ?? Dhikr(
                            stableKey: stableKey,
                            title: seedItem.title,
                            text: seedItem.text,
                            repeatCount: seedItem.count,
                            virtue: seedItem.virtue,
                            source: seedItem.source,
                            sortOrder: itemIndex,
                            category: category
                        )
                    if item.modelContext == nil { context.insert(item) }
                    item.title = seedItem.title
                    item.text = seedItem.text
                    item.repeatCount = seedItem.count
                    item.virtue = seedItem.virtue
                    item.source = seedItem.source
                    item.sortOrder = itemIndex
                    item.category = category
                    seededItems.append(item)
                    retainedItemKeys.insert(stableKey)
                    retainedRepeatCounts[stableKey] = seedItem.count
                }
                category.items = seededItems
            }

            for item in existingItems where !retainedItemKeys.contains(item.stableKey) {
                context.delete(item)
            }
            for category in existingCategories where !retainedCategoryKeys.contains(category.key) {
                context.delete(category)
            }
            for favorite in try context.fetch(FetchDescriptor<FavoriteDhikr>())
            where !retainedItemKeys.contains(favorite.dhikrStableKey) {
                context.delete(favorite)
            }
            for count in try context.fetch(FetchDescriptor<DhikrDailyCount>()) {
                if let repeatCount = retainedRepeatCounts[count.dhikrStableKey] {
                    count.count = min(max(0, count.count), repeatCount)
                } else {
                    context.delete(count)
                }
            }
            try context.save()
        }
    }
}

private func stableDhikrKey(
    categoryKey: String,
    itemKey: String?,
    index: Int
) throws -> String {
    let resolvedItemKey = itemKey ?? "item_\(index + 1)"
    guard !resolvedItemKey.isEmpty, !resolvedItemKey.contains("/") else {
        throw AdhkarLibraryError.invalidItemKey(category: categoryKey, index: index)
    }
    return "\(categoryKey)/\(resolvedItemKey)"
}

private struct SeedDocument: Decodable {
    let categories: [SeedCategory]
}

private struct SeedCategory: Decodable {
    let key: String
    let title: String
    let iconKey: String
    let items: [SeedDhikr]
}

private struct SeedDhikr: Decodable {
    let key: String?
    let title: String?
    let text: String
    let count: Int
    let virtue: String?
    let source: String?
}
