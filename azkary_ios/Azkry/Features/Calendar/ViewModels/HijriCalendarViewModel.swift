import Foundation
import Observation

@MainActor
@Observable
final class HijriCalendarViewModel {
    private(set) var month = HijriMonth(title: "", year: 0, cells: [])
    private var monthOffset = 0

    func load() { rebuild() }
    func previous() { monthOffset -= 1; rebuild() }
    func next() { monthOffset += 1; rebuild() }
    func today() { monthOffset = 0; rebuild() }

    private func rebuild() {
        var calendar = Calendar(identifier: .islamicUmmAlQura)
        calendar.locale = Locale(identifier: "ar")
        guard
            let target = calendar.date(byAdding: .month, value: monthOffset, to: .now),
            let interval = calendar.dateInterval(of: .month, for: target)
        else { return }

        let dayCount = calendar.range(of: .day, in: .month, for: target)?.count ?? 30
        let weekday = calendar.component(.weekday, from: interval.start)
        let leading = (weekday - calendar.firstWeekday + 7) % 7
        let current = calendar.dateComponents([.year, .month, .day], from: .now)
        let targetParts = calendar.dateComponents([.year, .month], from: target)

        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: "ar")
        formatter.calendar = calendar
        formatter.dateFormat = "MMMM"

        var cells = (0..<leading).map { HijriDayCell(id: $0, day: nil, isToday: false) }
        for day in 1...dayCount {
            let isToday = targetParts.year == current.year
                && targetParts.month == current.month
                && day == current.day
            cells.append(HijriDayCell(id: leading + day - 1, day: day, isToday: isToday))
        }
        month = HijriMonth(
            title: formatter.string(from: target),
            year: targetParts.year ?? 0,
            cells: cells
        )
    }
}
