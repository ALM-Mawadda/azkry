import Observation

@MainActor
@Observable
final class FridayViewModel {
    private(set) var completed = Set<FridaySunnah>()
    private let service: any FridayServiceProtocol
    private var dateKey = ""

    init(service: any FridayServiceProtocol = DefaultsFridayService()) {
        self.service = service
    }

    func load() {
        dateKey = LocalDateKey.make()
        completed = service.completed(dateKey: dateKey)
    }

    func toggle(_ sunnah: FridaySunnah) {
        let shouldComplete = !completed.contains(sunnah)
        service.set(sunnah, completed: shouldComplete, dateKey: dateKey)
        if shouldComplete { completed.insert(sunnah) } else { completed.remove(sunnah) }
    }
}
