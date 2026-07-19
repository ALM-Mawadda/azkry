import Foundation
import SwiftData

@Model
final class DhikrDailyCount {
    @Attribute(.unique) var recordKey: String
    var dateKey: String
    var dhikrStableKey: String
    var count: Int

    init(dateKey: String, dhikrStableKey: String, count: Int = 0) {
        self.recordKey = "\(dateKey)|\(dhikrStableKey)"
        self.dateKey = dateKey
        self.dhikrStableKey = dhikrStableKey
        self.count = count
    }
}

@Model
final class PrayerLog {
    @Attribute(.unique) var recordKey: String
    var dateKey: String
    var prayerRawValue: String
    var completed: Bool

    init(dateKey: String, prayer: Prayer, completed: Bool = false) {
        self.recordKey = "\(dateKey)|\(prayer.rawValue)"
        self.dateKey = dateKey
        self.prayerRawValue = prayer.rawValue
        self.completed = completed
    }
}

@Model
final class WorshipLog {
    @Attribute(.unique) var recordKey: String
    var dateKey: String
    var kind: String
    var completed: Bool

    init(dateKey: String, kind: String, completed: Bool = false) {
        self.recordKey = "\(dateKey)|\(kind)"
        self.dateKey = dateKey
        self.kind = kind
        self.completed = completed
    }
}

@Model
final class FavoriteDhikr {
    @Attribute(.unique) var dhikrStableKey: String
    var createdAt: Date

    init(dhikrStableKey: String, createdAt: Date = .now) {
        self.dhikrStableKey = dhikrStableKey
        self.createdAt = createdAt
    }
}
