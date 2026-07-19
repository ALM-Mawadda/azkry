@MainActor
protocol PrayerReminderServiceProtocol {
    func authorization() async -> ReminderAuthorization
    func isEnabled() async -> Bool
    func setEnabled(_ enabled: Bool) async throws
    func rescheduleIfEnabled() async
}
