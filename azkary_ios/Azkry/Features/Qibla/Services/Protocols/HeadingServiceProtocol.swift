import Foundation

@MainActor
protocol HeadingServiceProtocol {
    func updates() -> AsyncThrowingStream<Double, any Error>
    func stop()
}
