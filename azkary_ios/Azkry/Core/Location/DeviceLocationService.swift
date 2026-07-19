import CoreLocation
import Foundation

enum DeviceLocationError: LocalizedError {
    case denied
    case unavailable
    case requestInProgress

    var errorDescription: String? {
        switch self {
        case .denied:
            "لم يُسمح لأذكاري باستخدام الموقع. يمكنك تغيير الإذن من إعدادات iPhone."
        case .unavailable:
            "تعذر تحديد موقعك الآن. حاول مرة أخرى في مكان مكشوف."
        case .requestInProgress:
            "يجري تحديد موقعك بالفعل."
        }
    }
}

@MainActor
final class DeviceLocationService: NSObject, DeviceLocationServiceProtocol, CLLocationManagerDelegate {
    private let manager: CLLocationManager
    private var continuation: CheckedContinuation<GeoLocation, any Error>?

    override init() {
        let manager = CLLocationManager()
        self.manager = manager
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyHundredMeters
    }

    func currentLocation() async throws -> GeoLocation {
        guard continuation == nil else { throw DeviceLocationError.requestInProgress }

        return try await withTaskCancellationHandler {
            try await withCheckedThrowingContinuation { continuation in
                self.continuation = continuation
                requestLocationForCurrentAuthorization()
            }
        } onCancel: {
            Task { @MainActor [weak self] in
                self?.finish(with: .failure(CancellationError()))
            }
        }
    }

    nonisolated func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        let status = manager.authorizationStatus
        Task { @MainActor [weak self] in
            self?.handleAuthorization(status)
        }
    }

    nonisolated func locationManager(
        _ manager: CLLocationManager,
        didUpdateLocations locations: [CLLocation]
    ) {
        guard let location = locations.last,
              location.horizontalAccuracy >= 0 else { return }
        let result = GeoLocation(
            latitude: location.coordinate.latitude,
            longitude: location.coordinate.longitude
        )
        Task { @MainActor [weak self] in
            self?.finish(with: .success(result))
        }
    }

    nonisolated func locationManager(_ manager: CLLocationManager, didFailWithError error: any Error) {
        Task { @MainActor [weak self] in
            self?.finish(with: .failure(DeviceLocationError.unavailable))
        }
    }

    private func requestLocationForCurrentAuthorization() {
        handleAuthorization(manager.authorizationStatus)
    }

    private func handleAuthorization(_ status: CLAuthorizationStatus) {
        guard continuation != nil else { return }
        switch status {
        case .authorizedAlways, .authorizedWhenInUse:
            manager.requestLocation()
        case .notDetermined:
            manager.requestWhenInUseAuthorization()
        case .denied, .restricted:
            finish(with: .failure(DeviceLocationError.denied))
        @unknown default:
            finish(with: .failure(DeviceLocationError.unavailable))
        }
    }

    private func finish(with result: Result<GeoLocation, any Error>) {
        manager.stopUpdatingLocation()
        guard let continuation else { return }
        self.continuation = nil
        continuation.resume(with: result)
    }
}
