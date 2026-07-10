package one.adverse.glider

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.systemGestureExclusion
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

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
    systemBackGestureExclusionEnabled: Boolean = true,
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
    var containerWidthPx by remember { mutableFloatStateOf(0f) }
    val pagerSwipeState = remember { GliderPagerSwipeState() }

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

    val panelDragTargetModifier = Modifier.gliderPanelDragTarget(
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

    val centerDragTargetModifier = Modifier.gliderPanelDragTarget(
        enabled = gesturesEnabled,
        drawerWidthPx = drawerWidthPx,
        containerWidthPx = { containerWidthPx },
        currentOffsetPx = { currentOffsetPx },
        pagerSwipeState = pagerSwipeState,
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
            .onSizeChanged { size ->
                containerWidthPx = size.width.toFloat()
            }
            .background(colors.background),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(drawerWidth)
                .graphicsLayer {
                    translationX = -drawerWidthPx + maxOf(0f, currentOffsetPx)
                }
                .background(colors.background)
                .then(panelDragTargetModifier),
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
                .background(colors.background)
                .then(panelDragTargetModifier),
        ) {
            rightPanel()
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = currentOffsetPx
                }
                .then(centerDragTargetModifier),
        ) {
            CompositionLocalProvider(LocalGliderPagerSwipeState provides pagerSwipeState) {
                content(state)
            }
        }

        if (gesturesEnabled && state.panel == GliderPanel.Center) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .fillMaxHeight()
                    .width(edgeSwipeWidth)
                    .then(Modifier.optionalSystemGestureExclusion(systemBackGestureExclusionEnabled))
                    .then(panelDragTargetModifier),
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .width(edgeSwipeWidth)
                    .then(Modifier.optionalSystemGestureExclusion(systemBackGestureExclusionEnabled))
                    .then(panelDragTargetModifier),
            )
        }
    }
}

private fun Modifier.optionalSystemGestureExclusion(enabled: Boolean): Modifier {
    return if (enabled) {
        systemGestureExclusion()
    } else {
        this
    }
}

private fun Modifier.gliderPanelDragTarget(
    enabled: Boolean,
    drawerWidthPx: Float,
    containerWidthPx: () -> Float = { 0f },
    currentOffsetPx: () -> Float,
    pagerSwipeState: GliderPagerSwipeState? = null,
    onDragStart: () -> Unit,
    onDragOffsetChange: (Float) -> Unit,
    onSettle: (GliderPanel) -> Unit,
    onCancel: () -> Unit,
): Modifier {
    if (!enabled) return this

    return pointerInput(enabled, drawerWidthPx) {
        fun settleDrag() {
            val releasedOffset = currentOffsetPx().coerceIn(-drawerWidthPx, drawerWidthPx)
            onSettle(settledGliderPanel(releasedOffset, drawerWidthPx))
        }

        fun shouldYieldToPager(startX: Float, dragX: Float): Boolean {
            val pager = pagerSwipeState ?: return false
            if (abs(currentOffsetPx()) > 1f) return false

            val width = containerWidthPx()
            if (width <= 0f) return false

            val middleStart = width / 6f
            val middleEnd = width * 5f / 6f
            if (startX !in middleStart..middleEnd) return false

            return if (dragX < 0f) {
                pager.canSwipeNext
            } else {
                pager.canSwipePrevious
            }
        }

        awaitEachGesture {
            val down = awaitFirstDown(
                requireUnconsumed = false,
                pass = PointerEventPass.Initial,
            )
            val startX = down.position.x
            var pointerId = down.id
            var totalX = 0f
            var totalY = 0f
            var dragging = false
            val touchSlop = viewConfiguration.touchSlop

            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Initial)
                val change = event.changes.firstOrNull { it.id == pointerId }
                    ?: event.changes.firstOrNull { it.pressed }

                if (change == null) {
                    if (dragging) settleDrag() else onCancel()
                    break
                }

                pointerId = change.id

                if (change.changedToUpIgnoreConsumed()) {
                    if (dragging) settleDrag() else onCancel()
                    break
                }

                val delta = change.positionChangeIgnoreConsumed()
                totalX += delta.x
                totalY += delta.y

                if (!dragging) {
                    val horizontal = abs(totalX) > touchSlop && abs(totalX) > abs(totalY) * 1.15f
                    val vertical = abs(totalY) > touchSlop && abs(totalY) > abs(totalX)

                    if (horizontal) {
                        if (shouldYieldToPager(startX = startX, dragX = totalX)) {
                            onCancel()
                            break
                        }

                        dragging = true
                        onDragStart()
                        val slop = if (totalX > 0f) touchSlop else -touchSlop
                        onDragOffsetChange(
                            (currentOffsetPx() + totalX - slop).coerceIn(-drawerWidthPx, drawerWidthPx),
                        )
                        change.consume()
                    } else if (vertical) {
                        onCancel()
                        break
                    }
                } else {
                    onDragOffsetChange(
                        (currentOffsetPx() + delta.x).coerceIn(-drawerWidthPx, drawerWidthPx),
                    )
                    change.consume()
                }
            }
        }
    }
}
