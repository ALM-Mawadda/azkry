import Foundation

struct DefaultsFridayService: FridayServiceProtocol, @unchecked Sendable {
    private let defaults: UserDefaults

    init(defaults: UserDefaults = .standard) {
        self.defaults = defaults
    }

    func completed(dateKey: String) -> Set<FridaySunnah> {
        let values = defaults.stringArray(forKey: key(dateKey)) ?? []
        return Set(values.compactMap(FridaySunnah.init(rawValue:)))
    }

    func set(_ sunnah: FridaySunnah, completed: Bool, dateKey: String) {
        var values = self.completed(dateKey: dateKey)
        if completed { values.insert(sunnah) } else { values.remove(sunnah) }
        defaults.set(values.map(\.rawValue).sorted(), forKey: key(dateKey))
    }

    private func key(_ dateKey: String) -> String { "\(StorageKeys.fridaySunnahPrefix)\(dateKey)" }
}
