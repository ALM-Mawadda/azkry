import Testing
@testable import Azkry

@Suite("Features/QiblaViewModel")
struct QiblaViewModelTests {
    @Test("Live heading rotates the qibla arrow relative to the phone")
    @MainActor
    func liveHeading() async {
        let configuration = PrayerLocationPreset.paris.configuration
        let headingService = FakeHeadingService(values: [100])
        let viewModel = QiblaViewModel(
            settingsService: FakeSettingsService(configuration: configuration),
            headingService: headingService
        )

        await viewModel.run()

        let expected = (QiblaMath.bearing(
            latitude: configuration.location.latitude,
            longitude: configuration.location.longitude
        ) - 100 + 360).truncatingRemainder(dividingBy: 360)
        #expect(abs(viewModel.qiblaRotation - expected) < 0.001)
        #expect(viewModel.heading == 100)
        #expect(headingService.didStop)
    }

    @Test("Unavailable compass keeps the static bearing and explains the fallback")
    @MainActor
    func unavailableCompass() async {
        let viewModel = QiblaViewModel(
            settingsService: FakeSettingsService(configuration: .mecca),
            headingService: FakeHeadingService(error: HeadingServiceError.unavailable)
        )

        await viewModel.run()

        #expect(viewModel.heading == nil)
        #expect(viewModel.compassMessage?.contains("من الشمال") == true)
    }
}

private struct FakeSettingsService: SettingsServiceProtocol {
    let configuration: PrayerConfiguration

    func prayerConfiguration() -> PrayerConfiguration { configuration }
    func savePrayerConfiguration(_ configuration: PrayerConfiguration) {}
}

@MainActor
private final class FakeHeadingService: HeadingServiceProtocol {
    let values: [Double]
    let error: (any Error)?
    private(set) var didStop = false

    init(values: [Double] = [], error: (any Error)? = nil) {
        self.values = values
        self.error = error
    }

    func updates() -> AsyncThrowingStream<Double, any Error> {
        AsyncThrowingStream<Double, any Error>(bufferingPolicy: .unbounded) { continuation in
            for value in values {
                continuation.yield(value)
            }
            if let error {
                continuation.finish(throwing: error)
            } else {
                continuation.finish()
            }
        }
    }

    func stop() { didStop = true }
}
