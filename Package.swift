// swift-tools-version: 5.9

import PackageDescription

let package = Package(
    name: "GliderNav",
    platforms: [
        .iOS(.v15),
        .macOS(.v13)
    ],
    products: [
        .library(
            name: "GliderNav",
            targets: ["GliderNav"]
        )
    ],
    targets: [
        .target(
            name: "GliderNav",
            path: "ios/library/Sources/GliderNav"
        ),
        .testTarget(
            name: "GliderNavTests",
            dependencies: ["GliderNav"],
            path: "ios/library/Tests/GliderNavTests"
        )
    ]
)
