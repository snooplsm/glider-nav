import SwiftUI

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
