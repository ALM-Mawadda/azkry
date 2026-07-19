import Foundation

enum QiblaMath {
    private static let kaabaLatitude = 21.4225
    private static let kaabaLongitude = 39.8262

    static func bearing(latitude: Double, longitude: Double) -> Double {
        let originLatitude = latitude * .pi / 180
        let destinationLatitude = kaabaLatitude * .pi / 180
        let longitudeDelta = (kaabaLongitude - longitude) * .pi / 180
        let y = sin(longitudeDelta) * cos(destinationLatitude)
        let x = cos(originLatitude) * sin(destinationLatitude)
            - sin(originLatitude) * cos(destinationLatitude) * cos(longitudeDelta)
        return (atan2(y, x) * 180 / .pi + 360).truncatingRemainder(dividingBy: 360)
    }
}
