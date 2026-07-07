package one.adverse.glider

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
    edgeSwipeWidth: Dp = 56.dp,
    gesturesEnabled: Boolean = true,
    colors: GliderColors = GliderColors(),
    leftPanel: @Composable () -> Unit,
    rightPanel: @Composable () -> Unit,
    content: @Composable (GliderScaffoldState) -> Unit,
) {
    val density = LocalDensity.current
    val drawerWidthPx = with(density) { drawerWidth.toPx() }

    val targetOffsetPx = when (state.panel) {
        GliderPanel.Left -> drawerWidthPx
        GliderPanel.Center -> 0f
        GliderPanel.Right -> -drawerWidthPx
    }
    var currentOffsetPx by remember { mutableFloatStateOf(targetOffsetPx) }
    var isDragging by remember { mutableStateOf(false) }

    LaunchedEffect(targetOffsetPx, drawerWidthPx, isDragging) {
        if (!isDragging) {
            animate(
                initialValue = currentOffsetPx.coerceIn(-drawerWidthPx, drawerWidthPx),
                targetValue = targetOffsetPx,
                animationSpec = tween(durationMillis = if (state.panel == GliderPanel.Center) 200 else 300),
            ) { value, _ ->
                currentOffsetPx = value.coerceIn(-drawerWidthPx, drawerWidthPx)
            }
        }
    }

    val dragTargetModifier = Modifier.gliderPanelDragTarget(
        enabled = gesturesEnabled,
        drawerWidthPx = drawerWidthPx,
        currentOffsetPx = { currentOffsetPx },
        onDragStart = {
            isDragging = true
        },
        onDragOffsetChange = { offset ->
            currentOffsetPx = offset
        },
        onSettle = { panel ->
            state.settleTo(panel)
            isDragging = false
        },
        onCancel = {
            isDragging = false
        },
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .then(dragTargetModifier),
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

        if (gesturesEnabled && state.panel == GliderPanel.Center) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(edgeSwipeWidth)
                    .systemGestureExclusion()
                    .then(dragTargetModifier),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(edgeSwipeWidth)
                    .systemGestureExclusion()
                    .then(dragTargetModifier),
            )
        }

        if (gesturesEnabled && state.panel != GliderPanel.Center) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = currentOffsetPx
                    }
                    .then(dragTargetModifier),
            )
        }
    }
}

private fun Modifier.gliderPanelDragTarget(
    enabled: Boolean,
    drawerWidthPx: Float,
    currentOffsetPx: () -> Float,
    onDragStart: () -> Unit,
    onDragOffsetChange: (Float) -> Unit,
    onSettle: (GliderPanel) -> Unit,
    onCancel: () -> Unit,
): Modifier {
    if (!enabled) return this

    return pointerInput(enabled, drawerWidthPx) {
        fun settleDrag() {
            val releasedOffset = currentOffsetPx().coerceIn(-drawerWidthPx, drawerWidthPx)
            val threshold = drawerWidthPx * 0.30f
            onSettle(
                when {
                    releasedOffset > threshold -> GliderPanel.Left
                    releasedOffset < -threshold -> GliderPanel.Right
                    else -> GliderPanel.Center
                },
            )
            onDragOffsetChange(0f)
        }

        detectHorizontalDragGestures(
            onDragStart = {
                onDragStart()
            },
            onHorizontalDrag = { change, dragAmount ->
                change.consume()
                onDragOffsetChange(
                    (currentOffsetPx() + dragAmount).coerceIn(-drawerWidthPx, drawerWidthPx),
                )
            },
            onDragEnd = {
                settleDrag()
            },
            onDragCancel = {
                onCancel()
            },
        )
    }
}
