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

public struct GliderScaffold<LeftPanel: View, Center: View, RightPanel: View>: View {
    @ObservedObject private var state: GliderScaffoldState
    private let drawerWidth: CGFloat
    private let edgeSwipeWidth: CGFloat
    private let gesturesEnabled: Bool
    private let theme: GliderTheme
    private let leftPanel: () -> LeftPanel
    private let center: (GliderScaffoldState) -> Center
    private let rightPanel: () -> RightPanel
    @State private var dragOffset: CGFloat = 0

    public init(
        state: GliderScaffoldState,
        drawerWidth: CGFloat = 280,
        edgeSwipeWidth: CGFloat = 56,
        gesturesEnabled: Bool = true,
        theme: GliderTheme = GliderTheme(),
        @ViewBuilder leftPanel: @escaping () -> LeftPanel,
        @ViewBuilder center: @escaping (GliderScaffoldState) -> Center,
        @ViewBuilder rightPanel: @escaping () -> RightPanel
    ) {
        _state = ObservedObject(wrappedValue: state)
        self.drawerWidth = drawerWidth
        self.edgeSwipeWidth = edgeSwipeWidth
        self.gesturesEnabled = gesturesEnabled
        self.theme = theme
        self.leftPanel = leftPanel
        self.center = center
        self.rightPanel = rightPanel
    }

    public var body: some View {
        GeometryReader { _ in
            let currentOffset = clampedOffset(baseOffset + dragOffset)

            ZStack {
                HStack(spacing: 0) {
                    leftPanel()
                        .frame(width: drawerWidth)
                        .offset(x: -drawerWidth + max(0, currentOffset))
                    Spacer(minLength: 0)
                }

                HStack(spacing: 0) {
                    Spacer(minLength: 0)
                    rightPanel()
                        .frame(width: drawerWidth)
                        .offset(x: drawerWidth + min(0, currentOffset))
                }

                centerContent(offset: currentOffset)

                if gesturesEnabled && state.panel == .center {
                    edgeDragTargets
                }

                if gesturesEnabled && state.panel != .center {
                    centerDragOverlay(offset: currentOffset)
                }
            }
            .background(theme.background.ignoresSafeArea())
            .animation(.easeInOut(duration: 0.22), value: state.panel)
        }
    }

    private var baseOffset: CGFloat {
        switch state.panel {
        case .left: return drawerWidth
        case .center: return 0
        case .right: return -drawerWidth
        }
    }

    private func clampedOffset(_ offset: CGFloat) -> CGFloat {
        min(drawerWidth, max(-drawerWidth, offset))
    }

    @ViewBuilder
    private func centerContent(offset: CGFloat) -> some View {
        let content = center(state)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .offset(x: offset)

        if gesturesEnabled {
            content.gliderPanelDragTarget(
                state: state,
                drawerWidth: drawerWidth,
                dragOffset: $dragOffset
            )
        } else {
            content
        }
    }

    private var edgeDragTargets: some View {
        HStack(spacing: 0) {
            Color.clear
                .frame(width: edgeSwipeWidth)
                .contentShape(Rectangle())
                .gliderPanelDragTarget(
                    state: state,
                    drawerWidth: drawerWidth,
                    dragOffset: $dragOffset
                )

            Spacer(minLength: 0)

            Color.clear
                .frame(width: edgeSwipeWidth)
                .contentShape(Rectangle())
                .gliderPanelDragTarget(
                    state: state,
                    drawerWidth: drawerWidth,
                    dragOffset: $dragOffset
                )
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private func centerDragOverlay(offset: CGFloat) -> some View {
        Color.clear
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .contentShape(Rectangle())
            .offset(x: offset)
            .gliderPanelDragTarget(
                state: state,
                drawerWidth: drawerWidth,
                dragOffset: $dragOffset
            )
    }
}

private struct GliderPanelDragTargetModifier: ViewModifier {
    @ObservedObject private var state: GliderScaffoldState
    private let drawerWidth: CGFloat
    @Binding private var dragOffset: CGFloat

    init(
        state: GliderScaffoldState,
        drawerWidth: CGFloat,
        dragOffset: Binding<CGFloat>
    ) {
        _state = ObservedObject(wrappedValue: state)
        self.drawerWidth = drawerWidth
        _dragOffset = dragOffset
    }

    @ViewBuilder
    func body(content: Content) -> some View {
        content.gesture(gliderDragGesture)
    }

    private var baseOffset: CGFloat {
        switch state.panel {
        case .left: return drawerWidth
        case .center: return 0
        case .right: return -drawerWidth
        }
    }

    private func clampedOffset(_ offset: CGFloat) -> CGFloat {
        min(drawerWidth, max(-drawerWidth, offset))
    }

    private var gliderDragGesture: some Gesture {
        DragGesture(minimumDistance: 16)
            .onChanged { value in
                let horizontal = value.translation.width
                let vertical = value.translation.height
                guard abs(horizontal) > abs(vertical) * 1.15 else { return }
                dragOffset = clampedOffset(baseOffset + horizontal) - baseOffset
            }
            .onEnded { value in
                let projected = baseOffset + value.predictedEndTranslation.width
                let threshold = drawerWidth * 0.30

                withAnimation(.easeInOut(duration: 0.22)) {
                    if projected > threshold {
                        state.settle(to: .left)
                    } else if projected < -threshold {
                        state.settle(to: .right)
                    } else {
                        state.settle(to: .center)
                    }
                    dragOffset = 0
                }
            }
    }
}

private extension View {
    func gliderPanelDragTarget(
        state: GliderScaffoldState,
        drawerWidth: CGFloat,
        dragOffset: Binding<CGFloat>
    ) -> some View {
        modifier(
            GliderPanelDragTargetModifier(
                state: state,
                drawerWidth: drawerWidth,
                dragOffset: dragOffset
            )
        )
    }
}

public struct GliderPager<Page: View, TabLabel: View>: View {
    private let tabs: [GliderTab]
    @Binding private var selection: Int
    private let theme: GliderTheme
    private let headerHeight: CGFloat
    private let tabBarVisible: Bool
    private let tabLabel: (GliderTab, Bool) -> TabLabel
    private let page: (Int) -> Page

    public init(
        tabs: [GliderTab],
        selection: Binding<Int>,
        theme: GliderTheme = GliderTheme(),
        headerHeight: CGFloat = 64,
        tabBarVisible: Bool = true,
        @ViewBuilder tabLabel: @escaping (GliderTab, Bool) -> TabLabel,
        @ViewBuilder page: @escaping (Int) -> Page
    ) {
        self.tabs = tabs
        _selection = selection
        self.theme = theme
        self.headerHeight = headerHeight
        self.tabBarVisible = tabBarVisible
        self.tabLabel = tabLabel
        self.page = page
    }

    public var body: some View {
        VStack(spacing: 0) {
            if tabBarVisible {
                GliderTabBar(
                    tabs: tabs,
                    selection: $selection,
                    theme: theme,
                    height: headerHeight,
                    tabLabel: tabLabel
                )
                .transition(.move(edge: .top).combined(with: .opacity))
            }

            TabView(selection: $selection) {
                ForEach(Array(tabs.indices), id: \.self) { index in
                    page(index)
                        .tag(index)
                }
            }
            #if os(iOS)
            .tabViewStyle(PageTabViewStyle(indexDisplayMode: .never))
            #endif
        }
        .background(theme.background)
    }
}

public extension GliderPager where TabLabel == DefaultGliderTabLabel {
    init(
        tabs: [GliderTab],
        selection: Binding<Int>,
        theme: GliderTheme = GliderTheme(),
        headerHeight: CGFloat = 64,
        tabBarVisible: Bool = true,
        @ViewBuilder page: @escaping (Int) -> Page
    ) {
        self.init(
            tabs: tabs,
            selection: selection,
            theme: theme,
            headerHeight: headerHeight,
            tabBarVisible: tabBarVisible,
            tabLabel: { tab, selected in
                DefaultGliderTabLabel(tab: tab, selected: selected, theme: theme)
            },
            page: page
        )
    }
}

public struct DefaultGliderTabLabel: View {
    private let tab: GliderTab
    private let selected: Bool
    private let theme: GliderTheme

    public init(tab: GliderTab, selected: Bool, theme: GliderTheme = GliderTheme()) {
        self.tab = tab
        self.selected = selected
        self.theme = theme
    }

    public var body: some View {
        VStack(spacing: 4) {
            if let systemImage = tab.systemImage {
                Image(systemName: systemImage)
                    .font(.system(size: 22, weight: selected ? .semibold : .regular))
            } else {
                Text(tab.title)
                    .font(.system(size: 15, weight: selected ? .bold : .medium))
            }
        }
        .foregroundColor(selected ? theme.selectedContent : theme.unselectedContent)
        .frame(maxWidth: .infinity)
        .frame(minHeight: 44)
        .contentShape(Rectangle())
    }
}

private struct GliderTabBar<TabLabel: View>: View {
    let tabs: [GliderTab]
    @Binding var selection: Int
    let theme: GliderTheme
    let height: CGFloat
    let tabLabel: (GliderTab, Bool) -> TabLabel

    var body: some View {
        HStack(spacing: 0) {
            ForEach(Array(tabs.enumerated()), id: \.element.id) { index, tab in
                Button {
                    withAnimation(.easeInOut(duration: 0.18)) {
                        selection = index
                    }
                } label: {
                    tabLabel(tab, selection == index)
                }
                .buttonStyle(.plain)
                .frame(maxWidth: .infinity)
            }
        }
        .frame(height: height)
        .overlay(alignment: .bottom) {
            HStack(spacing: 0) {
                ForEach(Array(tabs.indices), id: \.self) { index in
                    Group {
                        if selection == index {
                            GliderBrushStroke(theme: theme)
                        } else {
                            Color.clear
                        }
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 4)
                }
            }
        }
    }
}

private struct GliderBrushStroke: View {
    let theme: GliderTheme

    var body: some View {
        Capsule()
            .fill(
                LinearGradient(
                    gradient: Gradient(colors: [
                        theme.gradientStart,
                        theme.gradientEnd,
                        theme.gradientStart
                    ]),
                    startPoint: .leading,
                    endPoint: .trailing
                )
            )
    }
}

public struct GliderGradientSlider: View {
    @Binding private var value: Double
    private let range: ClosedRange<Double>
    private let gamma: Double
    private let theme: GliderTheme
    private let trackHeight: CGFloat
    private let thumbSize: CGFloat

    public init(
        value: Binding<Double>,
        range: ClosedRange<Double>,
        gamma: Double = 1.0,
        theme: GliderTheme = GliderTheme(),
        trackHeight: CGFloat = 22,
        thumbSize: CGFloat = 34
    ) {
        _value = value
        self.range = range
        self.gamma = gamma
        self.theme = theme
        self.trackHeight = trackHeight
        self.thumbSize = thumbSize
    }

    public var body: some View {
        GeometryReader { geometry in
            let width = max(1, geometry.size.width)
            let safeGamma = gamma == 0 ? 1.0 : gamma
            let normalized = min(max((value - range.lowerBound) / (range.upperBound - range.lowerBound), 0), 1)
            let progress = safeGamma == 1.0 ? normalized : pow(normalized, 1.0 / safeGamma)

            ZStack(alignment: .leading) {
                Capsule()
                    .fill(
                        LinearGradient(
                            gradient: Gradient(colors: [theme.gradientStart, theme.gradientEnd]),
                            startPoint: .leading,
                            endPoint: .trailing
                        )
                    )
                    .frame(height: trackHeight)

                Circle()
                    .fill(Color.white)
                    .frame(width: thumbSize, height: thumbSize)
                    .offset(x: progress * max(0, width - thumbSize))
            }
            .contentShape(Rectangle())
            .gesture(
                DragGesture(minimumDistance: 0)
                    .onChanged { gesture in
                        let x = min(max(0, gesture.location.x), width)
                        let position = x / width
                        let mapped = safeGamma == 1.0 ? position : pow(position, safeGamma)
                        value = range.lowerBound + ((range.upperBound - range.lowerBound) * mapped)
                    }
            )
        }
        .frame(height: max(trackHeight, thumbSize))
    }
}
