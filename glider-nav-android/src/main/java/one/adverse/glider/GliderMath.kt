package one.adverse.glider

import kotlin.math.abs
import kotlin.math.pow

internal fun gammaExponent(gamma: Float): Float = when {
    gamma.isNaN() || gamma == 0f -> 1f
    gamma < 0f -> 1f / abs(gamma)
    else -> gamma
}

internal fun valueToSliderPosition(
    value: Float,
    min: Float,
    max: Float,
    gamma: Float,
): Float {
    val range = max - min
    if (range == 0f || range.isNaN()) return 0f
    val exp = gammaExponent(gamma)
    val t = ((value - min) / range).coerceIn(0f, 1f)
    return if (exp == 1f) t else t.toDouble().pow(1.0 / exp).toFloat()
}

internal fun sliderPositionToValue(
    position: Float,
    min: Float,
    max: Float,
    gamma: Float,
): Float {
    val range = max - min
    if (range == 0f || range.isNaN()) return min
    val exp = gammaExponent(gamma)
    val p = position.coerceIn(0f, 1f)
    return min + (p.toDouble().pow(exp.toDouble()).toFloat() * range)
}

internal fun settledGliderPanel(releasedOffsetPx: Float, drawerWidthPx: Float): GliderPanel {
    if (drawerWidthPx <= 0f || drawerWidthPx.isNaN()) return GliderPanel.Center

    val threshold = drawerWidthPx * 0.30f
    return when {
        releasedOffsetPx > threshold -> GliderPanel.Left
        releasedOffsetPx < -threshold -> GliderPanel.Right
        else -> GliderPanel.Center
    }
}
