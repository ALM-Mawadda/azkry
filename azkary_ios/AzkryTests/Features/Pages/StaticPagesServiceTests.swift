import Testing
@testable import Azkry

@Suite("Features/StaticPagesService")
struct StaticPagesServiceTests {
    @Test("Pages hub exposes every reference topic")
    func topics() throws {
        let service = StaticPagesService()
        #expect(service.pages().count == PageKey.allCases.count)
        #expect(try #require(service.page(key: .names)).names.count == 99)
        #expect(!service.sayyidIstighfar().body.isEmpty)
    }
}
