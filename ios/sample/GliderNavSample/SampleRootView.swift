import SwiftUI
import GliderNav

struct SampleRootView: View {
    let autoDemo: Bool

    @StateObject private var glider = GliderScaffoldState()
    @State private var selection = 0
    @State private var assist = 48.0
    @State private var regen = 22.0

    private let theme = GliderTheme(
        background: Color(red: 0.07, green: 0.07, blue: 0.16),
        gradientStart: Color(red: 0.22, green: 0.84, blue: 0.49),
        gradientEnd: Color(red: 1.0, green: 0.18, blue: 0.56)
    )

    private let tabs = [
        GliderTab(id: "monitor", title: "Monitor", systemImage: "waveform.path.ecg"),
        GliderTab(id: "settings", title: "Settings", systemImage: "slider.horizontal.3"),
        GliderTab(id: "battery", title: "Battery", systemImage: "battery.100")
    ]

    var body: some View {
        GliderScaffold(
            state: glider,
            drawerWidth: 300,
            theme: theme,
            leftPanel: { SamplePanel(title: "Controller", tint: theme.gradientStart) },
            center: { state in
                VStack(spacing: 0) {
                    topBar(state: state)
                    GliderPager(
                        tabs: tabs,
                        selection: $selection,
                        theme: theme
                    ) { page in
                        pageContent(page)
                    }
                }
                .background(theme.background)
            },
            rightPanel: { SamplePanel(title: "Ride Tools", tint: theme.gradientEnd) }
        )
        .task {
            guard autoDemo else { return }
            await runAutoDemo()
        }
    }

    init(autoDemo: Bool = false) {
        self.autoDemo = autoDemo
    }

    @ViewBuilder
    private func pageContent(_ page: Int) -> some View {
        switch page {
        case 0:
            monitorPage
        case 1:
            settingsPage
        default:
            batteryPage
        }
    }

    private func topBar(state: GliderScaffoldState) -> some View {
        HStack {
            Button("Controls") {
                state.toggleLeft()
            }
            .buttonStyle(.borderedProminent)

            Spacer()

            Text("GliderNav")
                .font(.system(size: 22, weight: .black))
                .foregroundStyle(.white)

            Spacer()

            Button("Ride") {
                state.toggleRight()
            }
            .buttonStyle(.borderedProminent)
        }
        .padding(.horizontal, 18)
        .padding(.vertical, 14)
    }

    @MainActor
    private func runAutoDemo() async {
        while !Task.isCancelled {
            await sleep(seconds: 0.7)
            withAnimation(.easeInOut(duration: 0.35)) {
                glider.openLeft()
            }

            await sleep(seconds: 1.15)
            withAnimation(.easeInOut(duration: 0.35)) {
                glider.close()
            }

            await sleep(seconds: 0.7)
            withAnimation(.easeInOut(duration: 0.25)) {
                selection = 1
                assist = 78
                regen = 36
            }

            await sleep(seconds: 1.05)
            withAnimation(.easeInOut(duration: 0.25)) {
                selection = 2
            }

            await sleep(seconds: 1.05)
            withAnimation(.easeInOut(duration: 0.35)) {
                glider.openRight()
            }

            await sleep(seconds: 1.15)
            withAnimation(.easeInOut(duration: 0.35)) {
                glider.close()
            }

            await sleep(seconds: 0.7)
            withAnimation(.easeInOut(duration: 0.25)) {
                selection = 0
                assist = 48
                regen = 22
            }
        }
    }

    private func sleep(seconds: Double) async {
        let nanoseconds = UInt64(seconds * 1_000_000_000)
        try? await Task.sleep(nanoseconds: nanoseconds)
    }

    private var monitorPage: some View {
        VStack(spacing: 16) {
            HStack(spacing: 16) {
                StatCard(label: "Speed", value: "42", unit: "mph")
                StatCard(label: "Battery", value: "78", unit: "%")
            }
            HStack(spacing: 16) {
                StatCard(label: "Motor", value: "109", unit: "F")
                StatCard(label: "Controller", value: "101", unit: "F")
            }
            RoundedRectangle(cornerRadius: 24)
                .fill(
                    LinearGradient(
                        colors: [theme.gradientStart, theme.gradientEnd],
                        startPoint: .leading,
                        endPoint: .trailing
                    )
                )
                .frame(height: 144)
                .overlay(alignment: .leading) {
                    Text("Animated tab indicator, sliding panels, and gamma-aware controls.")
                        .font(.system(size: 22, weight: .black))
                        .foregroundStyle(.white)
                        .padding(20)
                }
            Spacer()
        }
        .padding(20)
    }

    private var settingsPage: some View {
        VStack(alignment: .leading, spacing: 22) {
            SliderBlock(label: "Assist", value: $assist, theme: theme)
            SliderBlock(label: "Regen", value: $regen, theme: theme)
            Text("Drag from either edge to open the left or right panel.")
                .foregroundStyle(Color(red: 0.72, green: 0.73, blue: 0.82))
            Spacer()
        }
        .padding(20)
    }

    private var batteryPage: some View {
        VStack(spacing: 14) {
            ForEach(0..<6) { index in
                HStack(spacing: 14) {
                    Text("Cell \(index + 1)")
                        .frame(width: 72, alignment: .leading)
                    ProgressView(value: 0.70 + (Double(index) * 0.04))
                        .tint(theme.gradientStart)
                    Text(String(format: "%.2f V", 3.72 + (Double(index) * 0.01)))
                }
                .foregroundStyle(.white)
                .padding(16)
                .background(Color.white.opacity(0.12), in: RoundedRectangle(cornerRadius: 18))
            }
            Spacer()
        }
        .padding(20)
    }
}

private struct SliderBlock: View {
    let label: String
    @Binding var value: Double
    let theme: GliderTheme

    var body: some View {
        VStack(spacing: 10) {
            HStack {
                Text(label)
                    .font(.headline.weight(.bold))
                Spacer()
                Text("\(Int(value))%")
            }
            .foregroundStyle(.white)

            GliderGradientSlider(
                value: $value,
                range: 0...100,
                gamma: 1.45,
                theme: theme
            )
        }
    }
}

private struct StatCard: View {
    let label: String
    let value: String
    let unit: String

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(label.uppercased())
                .font(.system(size: 12, weight: .bold))
                .foregroundStyle(Color(red: 0.72, green: 0.73, blue: 0.82))
            HStack(alignment: .lastTextBaseline, spacing: 6) {
                Text(value)
                    .font(.system(size: 44, weight: .black))
                Text(unit)
                    .font(.system(size: 18, weight: .bold))
                    .foregroundStyle(Color(red: 0.25, green: 0.89, blue: 0.60))
            }
            .foregroundStyle(.white)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(18)
        .background(Color.white.opacity(0.12), in: RoundedRectangle(cornerRadius: 24))
        .overlay(
            RoundedRectangle(cornerRadius: 24)
                .stroke(Color.white.opacity(0.15), lineWidth: 1)
        )
    }
}

private struct SamplePanel: View {
    let title: String
    let tint: Color

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                Circle()
                    .fill(tint)
                    .frame(width: 58, height: 58)
                Text(title)
                    .font(.system(size: 24, weight: .black))
                    .foregroundStyle(.white)

                ForEach(["Telemetry", "Settings", "Profiles", "Export", "Share"], id: \.self) { item in
                    Text(item)
                        .font(.system(size: 18))
                        .foregroundStyle(Color(red: 0.90, green: 0.89, blue: 0.95))
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(16)
                        .background(Color.white.opacity(0.08), in: RoundedRectangle(cornerRadius: 16))
                }
            }
            .padding(22)
        }
        .background(Color(red: 0.09, green: 0.09, blue: 0.20))
    }
}
