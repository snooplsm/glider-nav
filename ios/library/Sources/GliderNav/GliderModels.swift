import SwiftUI

public enum GliderPanel: Equatable {
    case left
    case center
    case right
}

@MainActor
public final class GliderScaffoldState: ObservableObject {
    @Published public private(set) var panel: GliderPanel

    public init(initialPanel: GliderPanel = .center) {
        self.panel = initialPanel
    }

    public func openLeft() {
        panel = .left
    }

    public func openRight() {
        panel = .right
    }

    public func close() {
        panel = .center
    }

    public func toggleLeft() {
        panel = panel == .left ? .center : .left
    }

    public func toggleRight() {
        panel = panel == .right ? .center : .right
    }

    public func settle(to panel: GliderPanel) {
        self.panel = panel
    }
}

public struct GliderTheme: Equatable {
    public var background: Color
    public var selectedContent: Color
    public var unselectedContent: Color
    public var gradientStart: Color
    public var gradientEnd: Color

    public init(
        background: Color = Color(red: 0x1B / 255, green: 0x1C / 255, blue: 0x38 / 255),
        selectedContent: Color = .white,
        unselectedContent: Color = Color(red: 0xE6 / 255, green: 0xE4 / 255, blue: 0xF2 / 255),
        gradientStart: Color = Color(red: 0x46 / 255, green: 0x49 / 255, blue: 0x9B / 255),
        gradientEnd: Color = Color(red: 0xDE / 255, green: 0x36 / 255, blue: 0x57 / 255)
    ) {
        self.background = background
        self.selectedContent = selectedContent
        self.unselectedContent = unselectedContent
        self.gradientStart = gradientStart
        self.gradientEnd = gradientEnd
    }
}

public struct GliderTab: Identifiable, Hashable {
    public var id: String
    public var title: String
    public var systemImage: String?

    public init(id: String, title: String, systemImage: String? = nil) {
        self.id = id
        self.title = title
        self.systemImage = systemImage
    }
}
