package one.adverse.glider

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Stable
class GliderScaffoldState(initialPanel: GliderPanel = GliderPanel.Center) {
    var panel by mutableStateOf(initialPanel)
        private set

    fun openLeft() {
        panel = GliderPanel.Left
    }

    fun openRight() {
        panel = GliderPanel.Right
    }

    fun close() {
        panel = GliderPanel.Center
    }

    fun settleTo(panel: GliderPanel) {
        this.panel = panel
    }

    fun toggleLeft() {
        panel = if (panel == GliderPanel.Left) GliderPanel.Center else GliderPanel.Left
    }

    fun toggleRight() {
        panel = if (panel == GliderPanel.Right) GliderPanel.Center else GliderPanel.Right
    }
}

@Composable
fun rememberGliderScaffoldState(
    initialPanel: GliderPanel = GliderPanel.Center,
): GliderScaffoldState = remember { GliderScaffoldState(initialPanel) }

@Composable
fun GliderScaffold(
    state: GliderScaffoldState,
    modifier: Modifier = Modifier,
    drawerWidth: Dp = 290.dp,
    gesturesEnabled: Boolean = true,
    colors: GliderColors = GliderColors(),
    leftPanel: @Composable () -> Unit,
    rightPanel: @Composable () -> Unit,
    content: @Composable (GliderScaffoldState) -> Unit,
) {
    val density = LocalDensity.current
    val drawerWidthPx = with(density) { drawerWidth.toPx() }
    var dragOffsetPx by remember { mutableFloatStateOf(0f) }

    val targetOffsetPx = when (state.panel) {
        GliderPanel.Left -> drawerWidthPx
        GliderPanel.Center -> 0f
        GliderPanel.Right -> -drawerWidthPx
    }
    val animatedBaseOffsetPx by animateFloatAsState(
        targetValue = targetOffsetPx,
        animationSpec = tween(durationMillis = if (state.panel == GliderPanel.Center) 200 else 300),
        label = "glider-panel-offset",
    )

    val currentOffsetPx = (animatedBaseOffsetPx + dragOffsetPx)
        .coerceIn(-drawerWidthPx, drawerWidthPx)

    val dragModifier = if (gesturesEnabled) {
        Modifier.draggable(
            orientation = Orientation.Horizontal,
            state = rememberDraggableState { delta ->
                dragOffsetPx = (dragOffsetPx + delta).coerceIn(
                    minimumValue = -drawerWidthPx - animatedBaseOffsetPx,
                    maximumValue = drawerWidthPx - animatedBaseOffsetPx,
                )
            },
            onDragStopped = { velocity ->
                val projectedOffset = currentOffsetPx + (velocity * 0.12f)
                val threshold = drawerWidthPx * 0.30f
                state.settleTo(
                    when {
                    projectedOffset > threshold -> GliderPanel.Left
                    projectedOffset < -threshold -> GliderPanel.Right
                    else -> GliderPanel.Center
                    },
                )
                dragOffsetPx = 0f
            },
        )
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .then(dragModifier),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(drawerWidth)
                .graphicsLayer {
                    translationX = -drawerWidthPx + maxOf(0f, currentOffsetPx)
                }
                .background(colors.background),
        ) {
            leftPanel()
        }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(drawerWidth)
                .align(Alignment.CenterEnd)
                .graphicsLayer {
                    translationX = drawerWidthPx + minOf(0f, currentOffsetPx)
                }
                .background(colors.background),
        ) {
            rightPanel()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = currentOffsetPx
                },
        ) {
            content(state)
        }
    }
}
