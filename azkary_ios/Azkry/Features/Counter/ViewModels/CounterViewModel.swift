import Observation

@MainActor
@Observable
final class CounterViewModel {
    private(set) var count = 0
    var target = 33 {
        didSet { persist() }
    }
    private let service: any CounterServiceProtocol

    init(service: any CounterServiceProtocol = DefaultsCounterService()) {
        self.service = service
    }

    func load() {
        let state = service.state()
        count = state.count
        target = state.target
    }

    func increment() {
        count += 1
        persist()
    }

    func reset() {
        count = 0
        persist()
    }

    private func persist() {
        service.save(CounterState(count: count, target: target))
    }
}
