# GliderNav iOS Sample

Generate and open the sample project:

```bash
xcodegen generate --spec ios/sample/GliderNavSample/project.yml
open ios/sample/GliderNavSample/GliderNavSample.xcodeproj
```

You can also create a new iOS app target in Xcode, add the local `GliderNav` package, then add these two Swift files to the app target.

The sample shows:

- `GliderScaffold` with left and right panels.
- `GliderPager` with three tabs.
- `GliderGradientSlider` controls using the shared theme.
