package one.adverse.glider

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GliderSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
    valueRange: ClosedFloatingPointRange<Float> = 0f..1f,
    gamma: Float = 1f,
    steps: Int = 0,
    trackHeight: Dp = 22.dp,
    thumbSize: Dp = 34.dp,
    colors: GliderColors = GliderColors(),
    sliderColors: SliderColors = SliderDefaults.colors(
        thumbColor = Color.Transparent,
        activeTrackColor = Color.Transparent,
        inactiveTrackColor = Color.Transparent,
    ),
) {
    val min = valueRange.start
    val max = valueRange.endInclusive
    val sliderPosition = remember(value, min, max, gamma) {
        valueToSliderPosition(value = value, min = min, max = max, gamma = gamma)
    }

    val containerHeight = if (trackHeight > thumbSize) trackHeight else thumbSize

    Box(modifier = modifier.height(containerHeight)) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .fillMaxWidth()
                .height(trackHeight)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(colors.gradientStart, colors.gradientEnd),
                    ),
                    shape = RoundedCornerShape(50),
                ),
        )

        Slider(
            value = sliderPosition,
            onValueChange = { position ->
                onValueChange(
                    sliderPositionToValue(
                        position = position,
                        min = min,
                        max = max,
                        gamma = gamma,
                    ),
                )
            },
            valueRange = 0f..1f,
            steps = steps,
            colors = sliderColors,
            thumb = {
                Box(
                    modifier = Modifier
                        .size(thumbSize)
                        .background(Color.White, CircleShape),
                )
            },
        )
    }
}
