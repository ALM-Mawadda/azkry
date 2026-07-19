import SwiftData

@MainActor
protocol AdhkarLibraryServiceProtocol {
    func prepareLibrary(in context: ModelContext) throws
    func categories(in context: ModelContext) throws -> [DhikrCategory]
}
