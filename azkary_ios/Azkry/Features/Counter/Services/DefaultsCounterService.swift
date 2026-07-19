import Foundation

struct DefaultsCounterService: CounterServiceProtocol, @unchecked Sendable {
    private let defaults: UserDefaults

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    func state() -> CounterState {
        CounterState(
            count: max(0, defaults.integer(forKey: StorageKeys.tasbihCount)),
            target: defaults.object(forKey: StorageKeys.tasbihTarget) == nil
                ? 33
                : defaults.integer(forKey: StorageKeys.tasbihTarget)
        )
    }

    func save(_ state: CounterState) {
        defaults.set(state.count, forKey: StorageKeys.tasbihCount)
        defaults.set(state.target, forKey: StorageKeys.tasbihTarget)
    }
}
