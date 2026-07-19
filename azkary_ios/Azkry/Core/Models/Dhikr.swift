import Foundation
import SwiftData

@Model
final class Dhikr {
    @Attribute(.unique) var stableKey: String
    var title: String?
    var text: String
    var repeatCount: Int
    var virtue: String?
    var source: String?
    var sortOrder: Int
    var category: DhikrCategory?

    init(
        stableKey: String,
        title: String?,
        text: String,
        repeatCount: Int,
        virtue: String?,
        source: String?,
        sortOrder: Int,
        category: DhikrCategory? = nil
    ) {
        self.stableKey = stableKey
        self.title = title
        self.text = text
        self.repeatCount = repeatCount
        self.virtue = virtue
        self.source = source
        self.sortOrder = sortOrder
        self.category = category
    }
}
