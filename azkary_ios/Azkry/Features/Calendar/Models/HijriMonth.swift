import Foundation

struct HijriDayCell: Identifiable, Hashable, Sendable {
    let id: Int
    let day: Int?
    let isToday: Bool
}

struct HijriMonth: Equatable, Sendable {
    let title: String
    let year: Int
    let cells: [HijriDayCell]
}
