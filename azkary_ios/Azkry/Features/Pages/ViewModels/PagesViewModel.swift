import Observation

@MainActor
@Observable
final class PagesViewModel {
    private(set) var pages: [AzkryPage] = []
    private(set) var sayyidIstighfar: PageSection?
    private(set) var selectedPage: AzkryPage?
    private let service: any PagesServiceProtocol

    init(service: any PagesServiceProtocol = StaticPagesService()) {
        self.service = service
    }

    func load() {
        pages = service.pages()
        sayyidIstighfar = service.sayyidIstighfar()
    }

    func load(key: PageKey) {
        selectedPage = service.page(key: key)
    }
}
