import Foundation

@MainActor
protocol DeviceLocationServiceProtocol {
    func currentLocation() async throws -> GeoLocation
}
