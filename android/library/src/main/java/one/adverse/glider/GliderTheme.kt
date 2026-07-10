package one.adverse.glider

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class GliderColors(
    val background: Color = Color(0xFF1B1C38),
    val selectedContent: Color = Color.White,
    val unselectedContent: Color = Color(0xFFE6E4F2),
    val gradientStart: Color = Color(0xFF46499B),
    val gradientEnd: Color = Color(0xFFDE3657),
)

@Immutable
data class GliderTab(
    val key: String,
    val contentDescription: String,
    val label: String = contentDescription,
)

enum class GliderPanel {
    Left,
    Center,
    Right,
}
