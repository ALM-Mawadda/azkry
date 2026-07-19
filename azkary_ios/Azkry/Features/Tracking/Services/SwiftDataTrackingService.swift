import SwiftData

@MainActor
final class SwiftDataTrackingService: TrackingServiceProtocol {
    func prayerState(dateKey: String, in context: ModelContext) throws -> [Prayer: Bool] {
        Dictionary(
            uniqueKeysWithValues: try context.fetch(FetchDescriptor<PrayerLog>())
                .filter { $0.dateKey == dateKey }
                .compactMap { row in
                    Prayer(rawValue: row.prayerRawValue).map { ($0, row.completed) }
                }
        )
    }

    func worshipState(dateKey: String, in context: ModelContext) throws -> [String: Bool] {
        Dictionary(
            uniqueKeysWithValues: try context.fetch(FetchDescriptor<WorshipLog>())
                .filter { $0.dateKey == dateKey }
                .map { ($0.kind, $0.completed) }
        )
    }

    func setPrayer(
        _ prayer: Prayer,
        completed: Bool,
        dateKey: String,
        in context: ModelContext
    ) throws {
        let key = "\(dateKey)|\(prayer.rawValue)"
        if let row = try context.fetch(FetchDescriptor<PrayerLog>()).first(where: { $0.recordKey == key }) {
            row.completed = completed
        } else {
            context.insert(PrayerLog(dateKey: dateKey, prayer: prayer, completed: completed))
        }
        try context.save()
    }

    func setWorship(
        _ kind: String,
        completed: Bool,
        dateKey: String,
        in context: ModelContext
    ) throws {
        let key = "\(dateKey)|\(kind)"
        if let row = try context.fetch(FetchDescriptor<WorshipLog>()).first(where: { $0.recordKey == key }) {
            row.completed = completed
        } else {
            context.insert(WorshipLog(dateKey: dateKey, kind: kind, completed: completed))
        }
        try context.save()
    }
}
