import XCTest
@testable import GliderNav

final class GliderNavTests: XCTestCase {
    func testTabIdentity() {
        let tab = GliderTab(id: "monitor", title: "Monitor", systemImage: "waveform.path.ecg")

        XCTAssertEqual(tab.id, "monitor")
        XCTAssertEqual(tab.title, "Monitor")
        XCTAssertEqual(tab.systemImage, "waveform.path.ecg")
    }

    func testGliderDragSettlesAfterThirtyPercentThreshold() {
        XCTAssertEqual(gliderSettledPanel(projectedOffset: 89, drawerWidth: 300), .center)
        XCTAssertEqual(gliderSettledPanel(projectedOffset: 91, drawerWidth: 300), .left)
        XCTAssertEqual(gliderSettledPanel(projectedOffset: -91, drawerWidth: 300), .right)
        XCTAssertEqual(gliderSettledPanel(projectedOffset: 50, drawerWidth: 0), .center)
    }
}
