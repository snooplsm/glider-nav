import XCTest
@testable import GliderNav

final class GliderNavTests: XCTestCase {
    func testTabIdentity() {
        let tab = GliderTab(id: "monitor", title: "Monitor", systemImage: "waveform.path.ecg")

        XCTAssertEqual(tab.id, "monitor")
        XCTAssertEqual(tab.title, "Monitor")
        XCTAssertEqual(tab.systemImage, "waveform.path.ecg")
    }
}
