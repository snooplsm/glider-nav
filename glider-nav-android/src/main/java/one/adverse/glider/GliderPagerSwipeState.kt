package one.adverse.glider

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

internal class GliderPagerSwipeState {
    var canSwipePrevious by mutableStateOf(false)
    var canSwipeNext by mutableStateOf(false)
}

internal val LocalGliderPagerSwipeState = compositionLocalOf<GliderPagerSwipeState?> { null }
