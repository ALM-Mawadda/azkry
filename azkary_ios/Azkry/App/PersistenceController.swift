import Observation
import SwiftData

@MainActor
@Observable
final class PersistenceController {
    private(set) var modelContainer: ModelContainer?
    private(set) var errorMessage: String?

    init() {
        openStore()
    }

    func retry() {
        openStore()
    }

    private func openStore() {
        do {
            modelContainer = try AzkrySchema.makeModelContainer()
            errorMessage = nil
        } catch {
            modelContainer = nil
            errorMessage = error.localizedDescription
        }
    }
}
