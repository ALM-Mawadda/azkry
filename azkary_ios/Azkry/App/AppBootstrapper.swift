import Observation
import SwiftData

@MainActor
@Observable
final class AppBootstrapper {
    enum State: Equatable {
        case idle
        case loading
        case ready
        case failed(String)
    }

    private(set) var state: State = .idle
    private let adhkarService: any AdhkarLibraryServiceProtocol

    init(adhkarService: (any AdhkarLibraryServiceProtocol)? = nil) {
        self.adhkarService = adhkarService ?? SwiftDataAdhkarLibraryService()
    }

    func start(in context: ModelContext) async {
        guard state == .idle else { return }
        state = .loading
        await Task.yield()

        do {
            try adhkarService.prepareLibrary(in: context)
            state = .ready
        } catch is CancellationError {
            state = .idle
        } catch {
            state = .failed(error.localizedDescription)
        }
    }

    func retry(in context: ModelContext) async {
        state = .idle
        await start(in: context)
    }
}
