package one.adverse.glider

import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GliderMathTest {
    @Test
    fun gammaMappingRoundTrips() {
        val min = 50f
        val max = 60_000f
        val gamma = 2.5f

        listOf(min, 100f, 1_000f, 12_500f, max).forEach { value ->
            val position = valueToSliderPosition(value, min, max, gamma)
            val mapped = sliderPositionToValue(position, min, max, gamma)
            assertTrue(abs(value - mapped) < 0.05f, "value=$value mapped=$mapped")
        }
    }

    @Test
    fun valuesAreClamped() {
        assertEquals(0f, valueToSliderPosition(-20f, 0f, 100f, 1f))
        assertEquals(1f, valueToSliderPosition(120f, 0f, 100f, 1f))
        assertEquals(0f, sliderPositionToValue(-1f, 0f, 100f, 1f))
        assertEquals(100f, sliderPositionToValue(2f, 0f, 100f, 1f))
    }

    @Test
    fun gliderDragSettlesAfterThirtyPercentThreshold() {
        assertEquals(GliderPanel.Center, settledGliderPanel(89f, 300f))
        assertEquals(GliderPanel.Left, settledGliderPanel(91f, 300f))
        assertEquals(GliderPanel.Right, settledGliderPanel(-91f, 300f))
        assertEquals(GliderPanel.Center, settledGliderPanel(50f, 0f))
    }
}
