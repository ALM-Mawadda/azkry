import Observation
import SwiftData

struct WorshipTask: Identifiable {
    let id: String
    let title: String
    let systemImage: String
}

@MainActor
@Observable
final class TrackingViewModel {
    static let tasks = [
        WorshipTask(id: "adhkar", title: "أذكار اليوم", systemImage: "sparkles"),
        WorshipTask(id: "quran", title: "ورد القرآن", systemImage: "book.closed.fill"),
        WorshipTask(id: "rawatib", title: "السنن الرواتب", systemImage: "moon.stars.fill"),
        WorshipTask(id: "charity", title: "صدقة أو عمل خير", systemImage: "heart.fill"),
    ]

    private(set) var prayers: [Prayer: Bool] = [:]
    private(set) var worship: [String: Bool] = [:]
    private(set) var errorMessage: String?
    private let service: any TrackingServiceProtocol
    private let dateKey = LocalDateKey.make()

    init(service: (any TrackingServiceProtocol)? = nil) {
        self.service = service ?? SwiftDataTrackingService()
    }

    var completedCount: Int {
        prayers.values.filter { $0 }.count + worship.values.filter { $0 }.count
    }

    var totalCount: Int { Prayer.obligatory.count + Self.tasks.count }

    func load(in context: ModelContext) {
        do {
            prayers = try service.prayerState(dateKey: dateKey, in: context)
            worship = try service.worshipState(dateKey: dateKey, in: context)
            errorMessage = nil
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func toggle(_ prayer: Prayer, in context: ModelContext) {
        let next = !(prayers[prayer] ?? false)
        do {
            try service.setPrayer(prayer, completed: next, dateKey: dateKey, in: context)
            prayers[prayer] = next
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func toggle(_ task: WorshipTask, in context: ModelContext) {
        let next = !(worship[task.id] ?? false)
        do {
            try service.setWorship(task.id, completed: next, dateKey: dateKey, in: context)
            worship[task.id] = next
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
