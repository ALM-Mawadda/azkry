import Observation

@MainActor
@Observable
final class QiblaViewModel {
    private(set) var city = PrayerConfiguration.mecca.city
    private(set) var bearing = QiblaMath.bearing(
        latitude: PrayerConfiguration.mecca.location.latitude,
        longitude: PrayerConfiguration.mecca.location.longitude
    )
    private(set) var heading: Double?
    private(set) var compassMessage: String?

    private let settingsService: any SettingsServiceProtocol
    private let headingService: any HeadingServiceProtocol

    init(
        settingsService: any SettingsServiceProtocol = DefaultsSettingsService(),
        headingService: any HeadingServiceProtocol = CompassHeadingService()
    ) {
        self.settingsService = settingsService
        self.headingService = headingService
    }

    var qiblaRotation: Double {
        Self.normalizedAngle(bearing - (heading ?? 0))
    }

    var compassRotation: Double { -(heading ?? 0) }

    var isAligned: Bool {
        guard heading != nil else { return false }
        let difference = abs(qiblaRotation > 180 ? qiblaRotation - 360 : qiblaRotation)
        return difference <= 5
    }

    func run() async {
        let configuration = settingsService.prayerConfiguration()
        city = configuration.city
        bearing = QiblaMath.bearing(
            latitude: configuration.location.latitude,
            longitude: configuration.location.longitude
        )
        defer { headingService.stop() }
        do {
            for try await value in headingService.updates() {
                guard !Task.isCancelled else { return }
                heading = Self.normalizedAngle(value)
                compassMessage = nil
            }
        } catch is CancellationError {
            return
        } catch {
            heading = nil
            compassMessage = "البوصلة غير متاحة؛ يعرض السهم زاوية القبلة من الشمال."
        }
    }

    private static func normalizedAngle(_ value: Double) -> Double {
        let remainder = value.truncatingRemainder(dividingBy: 360)
        return remainder < 0 ? remainder + 360 : remainder
    }
}
