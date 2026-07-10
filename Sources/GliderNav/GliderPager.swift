import SwiftUI

public struct GliderPager<Page: View, TabLabel: View>: View {
    private let tabs: [GliderTab]
    @Binding private var selection: Int
    private let theme: GliderTheme
    private let headerHeight: CGFloat
    private let tabBarVisible: Bool
    private let tabLabel: (GliderTab, Bool) -> TabLabel
    private let page: (Int) -> Page
    @Environment(\.gliderPagerSwipeState) private var gliderPagerSwipeState

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
        .onAppear(perform: updatePagerSwipeState)
        .onChange(of: selection) { _ in
            updatePagerSwipeState()
        }
        .onChange(of: tabs.count) { _ in
            updatePagerSwipeState()
        }
        .onDisappear {
            gliderPagerSwipeState?.clear()
        }
    }

    private func updatePagerSwipeState() {
        gliderPagerSwipeState?.update(selection: selection, count: tabs.count)
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
