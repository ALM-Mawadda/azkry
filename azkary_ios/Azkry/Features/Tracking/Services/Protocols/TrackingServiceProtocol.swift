import SwiftData

@MainActor
protocol TrackingServiceProtocol {
    func prayerState(dateKey: String, in context: ModelContext) throws -> [Prayer: Bool]
    func worshipState(dateKey: String, in context: ModelContext) throws -> [String: Bool]
    func setPrayer(_ prayer: Prayer, completed: Bool, dateKey: String, in context: ModelContext) throws
    func setWorship(_ kind: String, completed: Bool, dateKey: String, in context: ModelContext) throws
}
