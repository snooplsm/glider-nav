import SwiftUI

internal func gliderSettledPanel(projectedOffset: CGFloat, drawerWidth: CGFloat) -> GliderPanel {
    guard drawerWidth > 0 else { return .center }

    let threshold = drawerWidth * 0.30
    if projectedOffset > threshold {
        return .left
    } else if projectedOffset < -threshold {
        return .right
    } else {
        return .center
    }
}

@MainActor
internal final class GliderPagerSwipeState {
    var canSwipePrevious = false
    var canSwipeNext = false

    func update(selection: Int, count: Int) {
        canSwipePrevious = selection > 0
        canSwipeNext = selection < max(0, count - 1)
    }

    func clear() {
        canSwipePrevious = false
        canSwipeNext = false
    }
}

internal struct GliderPagerSwipeStateKey: EnvironmentKey {
    static let defaultValue: GliderPagerSwipeState? = nil
}

internal extension EnvironmentValues {
    var gliderPagerSwipeState: GliderPagerSwipeState? {
        get { self[GliderPagerSwipeStateKey.self] }
        set { self[GliderPagerSwipeStateKey.self] = newValue }
    }
}
