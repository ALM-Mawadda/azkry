import XCTest

final class AzkryLaunchTests: XCTestCase {
    override func setUp() {
        super.setUp()
        continueAfterFailure = false
    }

    @MainActor
    func testArabicHomeLaunches() {
        let app = XCUIApplication()
        app.launch()

        XCTAssertTrue(app.staticTexts["أذكاري"].waitForExistence(timeout: 8))
    }

}
