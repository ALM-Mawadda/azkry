protocol SettingsServiceProtocol: Sendable {
    func prayerConfiguration() -> PrayerConfiguration
    func savePrayerConfiguration(_ configuration: PrayerConfiguration)
}
