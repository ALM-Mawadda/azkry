import CoreLocation
import Foundation

enum HeadingServiceError: LocalizedError {
    case unavailable

    var errorDescription: String? {
        "البوصلة غير متاحة على هذا الجهاز."
    }
}

@MainActor
final class CompassHeadingService: NSObject, HeadingServiceProtocol, CLLocationManagerDelegate {
    private let manager: CLLocationManager
    private var continuation: AsyncThrowingStream<Double, any Error>.Continuation?

    override init() {
        let manager = CLLocationManager()
        self.manager = manager
        super.init()
        manager.delegate = self
        manager.headingFilter = 1
    }

    func updates() -> AsyncThrowingStream<Double, any Error> {
        AsyncThrowingStream(bufferingPolicy: .bufferingNewest(1)) { continuation in
            self.continuation?.finish()
            self.continuation = continuation
            continuation.onTermination = { @Sendable [weak self] _ in
                Task { @MainActor in self?.stop() }
            }
            guard CLLocationManager.headingAvailable() else {
                self.stop(with: HeadingServiceError.unavailable)
                return
            }
            manager.startUpdatingHeading()
        }
    }

    func stop() {
        manager.stopUpdatingHeading()
        continuation?.finish()
        continuation = nil
    }

    nonisolated func locationManager(
        _ manager: CLLocationManager,
        didUpdateHeading newHeading: CLHeading
    ) {
        guard newHeading.headingAccuracy >= 0 else { return }
        let heading = newHeading.trueHeading >= 0
            ? newHeading.trueHeading
            : newHeading.magneticHeading
        Task { @MainActor [weak self] in
            self?.continuation?.yield(heading)
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: any Error) {
        Task { @MainActor [weak self] in
            self?.stop(with: HeadingServiceError.unavailable)
        }
    }

    private func stop(with error: any Error) {
        manager.stopUpdatingHeading()
        continuation?.finish(throwing: error)
        continuation = nil
    }
}
