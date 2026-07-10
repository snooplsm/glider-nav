import SwiftUI

public struct GliderScaffold<LeftPanel: View, Center: View, RightPanel: View>: View {
    @ObservedObject private var state: GliderScaffoldState
    private let drawerWidth: CGFloat
    private let edgeSwipeWidth: CGFloat
    private let gesturesEnabled: Bool
    private let theme: GliderTheme
    private let leftPanel: () -> LeftPanel
    private let center: (GliderScaffoldState) -> Center
    private let rightPanel: () -> RightPanel
    @State private var currentOffset: CGFloat = 0
    @State private var isDragging = false
    @State private var pagerSwipeState = GliderPagerSwipeState()

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
        GeometryReader { geometry in
            let visibleOffset = clampedOffset(currentOffset)
            let containerWidth = geometry.size.width

            ZStack {
                HStack(spacing: 0) {
                    leftPanel()
                        .frame(width: drawerWidth)
                        .offset(x: -drawerWidth + max(0, visibleOffset))
                    Spacer(minLength: 0)
                }

                HStack(spacing: 0) {
                    Spacer(minLength: 0)
                    rightPanel()
                        .frame(width: drawerWidth)
                        .offset(x: drawerWidth + min(0, visibleOffset))
                }

                centerContent(offset: visibleOffset, containerWidth: containerWidth)

                if gesturesEnabled && state.panel == .center {
                    edgeDragTargets
                }

                if gesturesEnabled && state.panel != .center {
                    centerDragOverlay(offset: visibleOffset)
                }
            }
            .background(theme.background.ignoresSafeArea())
            .onAppear {
                currentOffset = panelOffset(for: state.panel)
            }
            .onChange(of: state.panel) { panel in
                guard !isDragging else { return }
                withAnimation(.easeInOut(duration: 0.22)) {
                    currentOffset = panelOffset(for: panel)
                }
            }
        }
    }

    private func panelOffset(for panel: GliderPanel) -> CGFloat {
        switch panel {
        case .left: return drawerWidth
        case .center: return 0
        case .right: return -drawerWidth
        }
    }

    private func clampedOffset(_ offset: CGFloat) -> CGFloat {
        min(drawerWidth, max(-drawerWidth, offset))
    }

    @ViewBuilder
    private func centerContent(offset: CGFloat, containerWidth: CGFloat) -> some View {
        let content = center(state)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .offset(x: offset)
            .environment(\.gliderPagerSwipeState, pagerSwipeState)

        if gesturesEnabled {
            content.gliderPanelDragTarget(
                state: state,
                drawerWidth: drawerWidth,
                containerWidth: containerWidth,
                pagerSwipeState: pagerSwipeState,
                currentOffset: $currentOffset,
                isDragging: $isDragging
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
                    containerWidth: nil,
                    pagerSwipeState: nil,
                    currentOffset: $currentOffset,
                    isDragging: $isDragging
                )

            Spacer(minLength: 0)

            Color.clear
                .frame(width: edgeSwipeWidth)
                .contentShape(Rectangle())
                .gliderPanelDragTarget(
                    state: state,
                    drawerWidth: drawerWidth,
                    containerWidth: nil,
                    pagerSwipeState: nil,
                    currentOffset: $currentOffset,
                    isDragging: $isDragging
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
                containerWidth: nil,
                pagerSwipeState: nil,
                currentOffset: $currentOffset,
                isDragging: $isDragging
            )
    }
}

private struct GliderPanelDragTargetModifier: ViewModifier {
    @ObservedObject private var state: GliderScaffoldState
    private let drawerWidth: CGFloat
    private let containerWidth: CGFloat?
    private let pagerSwipeState: GliderPagerSwipeState?
    @Binding private var currentOffset: CGFloat
    @Binding private var isDragging: Bool
    @State private var dragStartOffset: CGFloat?

    init(
        state: GliderScaffoldState,
        drawerWidth: CGFloat,
        containerWidth: CGFloat?,
        pagerSwipeState: GliderPagerSwipeState?,
        currentOffset: Binding<CGFloat>,
        isDragging: Binding<Bool>
    ) {
        _state = ObservedObject(wrappedValue: state)
        self.drawerWidth = drawerWidth
        self.containerWidth = containerWidth
        self.pagerSwipeState = pagerSwipeState
        _currentOffset = currentOffset
        _isDragging = isDragging
    }

    @ViewBuilder
    func body(content: Content) -> some View {
        content.simultaneousGesture(gliderDragGesture)
    }

    private func panelOffset(for panel: GliderPanel) -> CGFloat {
        switch panel {
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
                if dragStartOffset == nil {
                    guard abs(horizontal) > abs(vertical) * 1.15 else { return }
                    if shouldYieldToPager(startX: value.startLocation.x, dragX: horizontal) {
                        return
                    }
                    dragStartOffset = currentOffset
                    isDragging = true
                }
                currentOffset = clampedOffset((dragStartOffset ?? currentOffset) + horizontal)
            }
            .onEnded { value in
                guard let dragStartOffset else {
                    isDragging = false
                    return
                }
                let projected = dragStartOffset + value.predictedEndTranslation.width
                let targetPanel = gliderSettledPanel(projectedOffset: projected, drawerWidth: drawerWidth)

                withAnimation(.easeInOut(duration: 0.22)) {
                    state.settle(to: targetPanel)
                    currentOffset = panelOffset(for: targetPanel)
                    isDragging = false
                }
                self.dragStartOffset = nil
        }
    }

    private func shouldYieldToPager(startX: CGFloat, dragX: CGFloat) -> Bool {
        guard let pagerSwipeState else { return false }
        guard abs(currentOffset) <= 1 else { return false }
        guard let containerWidth, containerWidth > 0 else { return false }

        let middleStart = containerWidth / 6
        let middleEnd = containerWidth * 5 / 6
        guard (middleStart...middleEnd).contains(startX) else { return false }

        return dragX < 0 ? pagerSwipeState.canSwipeNext : pagerSwipeState.canSwipePrevious
    }
}

private extension View {
    func gliderPanelDragTarget(
        state: GliderScaffoldState,
        drawerWidth: CGFloat,
        containerWidth: CGFloat?,
        pagerSwipeState: GliderPagerSwipeState?,
        currentOffset: Binding<CGFloat>,
        isDragging: Binding<Bool>
    ) -> some View {
        modifier(
            GliderPanelDragTargetModifier(
                state: state,
                drawerWidth: drawerWidth,
                containerWidth: containerWidth,
                pagerSwipeState: pagerSwipeState,
                currentOffset: currentOffset,
                isDragging: isDragging
            )
        )
    }
}
