import SwiftUI
import GliderNav

@main
struct GliderNavSampleApp: App {
    var body: some Scene {
        WindowGroup {
            SampleRootView(autoDemo: ProcessInfo.processInfo.arguments.contains("--demo-video"))
        }
    }
}
