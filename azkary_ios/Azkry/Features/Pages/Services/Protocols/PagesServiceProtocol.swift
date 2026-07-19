protocol PagesServiceProtocol: Sendable {
    func pages() -> [AzkryPage]
    func page(key: PageKey) -> AzkryPage?
    func sayyidIstighfar() -> PageSection
}
