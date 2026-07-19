import Observation
import SwiftData

@MainActor
@Observable
final class AdhkarLibraryViewModel {
    private(set) var categories: [DhikrCategory] = []
    private(set) var errorMessage: String?
    private let service: any AdhkarContentServiceProtocol

    init(service: (any AdhkarContentServiceProtocol)? = nil) {
        self.service = service ?? SwiftDataAdhkarContentService()
    }

    func load(in context: ModelContext, includeExclusive: Bool = false) {
        do {
            categories = try service.categories(in: context, includeExclusive: includeExclusive)
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}

@MainActor
@Observable
final class DhikrReaderViewModel {
    private(set) var category: DhikrCategory?
    private(set) var counts: [String: Int] = [:]
    private(set) var favoriteKeys = Set<String>()
    private(set) var errorMessage: String?
    private let service: any AdhkarContentServiceProtocol

    init(service: (any AdhkarContentServiceProtocol)? = nil) {
        self.service = service ?? SwiftDataAdhkarContentService()
    }

    var orderedItems: [Dhikr] {
        category?.items.sorted(by: { $0.sortOrder < $1.sortOrder }) ?? []
    }

    func load(categoryKey: String, in context: ModelContext) {
        do {
            category = try service.category(key: categoryKey, in: context)
            counts = try service.counts(dateKey: LocalDateKey.make(), in: context)
            favoriteKeys = try service.favoriteKeys(in: context)
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func increment(_ dhikr: Dhikr, in context: ModelContext) {
        do {
            counts[dhikr.stableKey] = try service.increment(
                dhikr,
                dateKey: LocalDateKey.make(),
                in: context
            )
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func toggleFavorite(_ dhikr: Dhikr, in context: ModelContext) {
        do {
            let isFavorite = try service.toggleFavorite(
                stableKey: dhikr.stableKey,
                in: context
            )
            if isFavorite {
                favoriteKeys.insert(dhikr.stableKey)
            } else {
                favoriteKeys.remove(dhikr.stableKey)
            }
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}

@MainActor
@Observable
final class FavoritesViewModel {
    private(set) var items: [Dhikr] = []
    private(set) var errorMessage: String?
    private let service: any AdhkarContentServiceProtocol

    init(service: (any AdhkarContentServiceProtocol)? = nil) {
        self.service = service ?? SwiftDataAdhkarContentService()
    }

    func load(in context: ModelContext) {
        do {
            let keys = try service.favoriteKeys(in: context)
            items = try service.allDhikr(in: context).filter { keys.contains($0.stableKey) }
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}

@MainActor
@Observable
final class AdhkarSearchViewModel {
    private(set) var results: [Dhikr] = []
    private(set) var surahResults: [SurahSummary] = []
    private(set) var errorMessage: String?
    var query = ""
    private var allItems: [Dhikr] = []
    private var allSurahs: [SurahSummary] = []
    private let service: any AdhkarContentServiceProtocol
    private let quranService: any QuranServiceProtocol

    init(
        service: (any AdhkarContentServiceProtocol)? = nil,
        quranService: (any QuranServiceProtocol)? = nil
    ) {
        self.service = service ?? SwiftDataAdhkarContentService()
        self.quranService = quranService ?? AssetQuranService()
    }

    var hasResults: Bool { !results.isEmpty || !surahResults.isEmpty }

    func load(in context: ModelContext) {
        do {
            allItems = try service.allDhikr(in: context)
            allSurahs = try quranService.index().surahs
            filter()
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func filter() {
        guard !query.isEmpty else {
            results = []
            surahResults = []
            return
        }
        results = allItems.filter { item in
            ArabicNormalization.contains(item.text, query: query)
                || ArabicNormalization.contains(item.title ?? "", query: query)
                || ArabicNormalization.contains(item.source ?? "", query: query)
        }
        surahResults = allSurahs.filter { surah in
            ArabicNormalization.contains(surah.name, query: query)
                || ArabicNormalization.contains(surah.englishName, query: query)
                || String(surah.number) == query.trimmingCharacters(in: .whitespaces)
        }
    }
}
