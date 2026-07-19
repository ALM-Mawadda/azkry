import Testing
@testable import Azkry

@Suite("Features/QiblaMath")
struct QiblaMathTests {
    @Test("Paris bearing points southeast toward Mecca")
    func parisBearing() {
        let bearing = QiblaMath.bearing(latitude: 48.8566, longitude: 2.3522)
        #expect(abs(bearing - 119.2) < 1)
    }

    @Test("Bearing is normalized into one turn")
    func normalized() {
        let bearing = QiblaMath.bearing(latitude: -33.8688, longitude: 151.2093)
        #expect((0..<360).contains(bearing))
    }
}
