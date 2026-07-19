protocol CounterServiceProtocol: Sendable {
    func state() -> CounterState
    func save(_ state: CounterState)
}
