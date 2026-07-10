package one.adverse.glider

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.TabPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp

@Composable
fun GliderTabIndicator(
    tabPositions: List<TabPosition>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    colors: GliderColors = GliderColors(),
) {
    val safeIndex = selectedIndex.coerceIn(0, tabPositions.lastIndex)
    val transition = updateTransition(safeIndex, label = "glider-tab-transition")

    val indicatorLeft by transition.animateDp(label = "glider-indicator-left") { index ->
        tabPositions.getOrNull(index)?.left ?: 0.dp
    }

    val indicatorRight by transition.animateDp(label = "glider-indicator-right") { index ->
        tabPositions.getOrNull(index)?.right ?: 0.dp
    }

    val infiniteTransition = rememberInfiniteTransition(label = "glider-border-shimmer")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 100f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "glider-shimmer-offset",
    )

    val animatedBrush = Brush.linearGradient(
        colors = listOf(
            colors.gradientStart,
            colors.gradientEnd,
            colors.gradientStart,
            colors.gradientEnd,
        ),
        start = Offset(offsetX, 0f),
        end = Offset(offsetX + 800f, 800f),
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentSize(Alignment.BottomStart),
    ) {
        Box(
            Modifier
                .offset(x = indicatorLeft)
                .width(indicatorRight - indicatorLeft)
                .height(4.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(animatedBrush),
        )
    }
}
