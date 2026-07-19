import Foundation
import SwiftData

@Model
final class DhikrCategory {
    @Attribute(.unique) var key: String
    var title: String
    var iconKey: String
    var sortOrder: Int
    @Relationship(deleteRule: .cascade, inverse: \Dhikr.category)
    var items: [Dhikr]

    init(key: String, title: String, iconKey: String, sortOrder: Int) {
        self.key = key
        self.title = title
        self.iconKey = iconKey
        self.sortOrder = sortOrder
        self.items = []
    }
}
