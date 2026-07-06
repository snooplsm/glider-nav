package one.adverse.glider.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import one.adverse.glider.GliderColors
import one.adverse.glider.GliderPager
import one.adverse.glider.GliderScaffold
import one.adverse.glider.GliderSlider
import one.adverse.glider.GliderTab
import one.adverse.glider.rememberGliderScaffoldState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                GliderSampleApp()
            }
        }
    }
}

@Composable
private fun GliderSampleApp() {
    val scaffoldState = rememberGliderScaffoldState()
    val colors = GliderColors(
        background = Color(0xFF111228),
        gradientStart = Color(0xFF39D67D),
        gradientEnd = Color(0xFFFF2F8F),
        unselectedContent = Color(0xFFB8B9D0),
    )

    GliderScaffold(
        state = scaffoldState,
        colors = colors,
        drawerWidth = 300.dp,
        leftPanel = { SamplePanel(title = "Controller", color = colors.gradientStart) },
        rightPanel = { SamplePanel(title = "Ride Tools", color = colors.gradientEnd) },
    ) { glider ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.background)
                .systemBarsPadding(),
        ) {
            SampleTopBar(
                onLeftClick = glider::toggleLeft,
                onRightClick = glider::toggleRight,
            )
            SamplePager(colors = colors)
        }
    }
}

@Composable
private fun SampleTopBar(
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Button(onClick = onLeftClick) {
            Text("Controls")
        }
        Text(
            text = "GliderNav",
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
        )
        Button(onClick = onRightClick) {
            Text("Ride")
        }
    }
}

@Composable
private fun SamplePager(colors: GliderColors) {
    var selectedIndex by remember { mutableIntStateOf(0) }
    val tabs = remember {
        listOf(
            GliderTab("monitor", "Monitor"),
            GliderTab("settings", "Settings"),
            GliderTab("battery", "Battery"),
        )
    }

    GliderPager(
        tabs = tabs,
        selectedIndex = selectedIndex,
        onSelectedIndexChange = { selectedIndex = it },
        colors = colors,
    ) { page ->
        when (page) {
            0 -> MonitorPage(colors)
            1 -> SettingsPage(colors)
            else -> BatteryPage(colors)
        }
    }
}

@Composable
private fun MonitorPage(colors: GliderColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(label = "Speed", value = "42", unit = "mph", modifier = Modifier.weight(1f))
            StatCard(label = "Battery", value = "78", unit = "%", modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            StatCard(label = "Motor", value = "109", unit = "F", modifier = Modifier.weight(1f))
            StatCard(label = "Controller", value = "101", unit = "F", modifier = Modifier.weight(1f))
        }
        SampleGradientCard(colors = colors)
    }
}

@Composable
private fun SettingsPage(colors: GliderColors) {
    var assist by remember { mutableFloatStateOf(48f) }
    var regen by remember { mutableFloatStateOf(22f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        SliderBlock(label = "Assist", value = assist, colors = colors, onValueChange = { assist = it })
        SliderBlock(label = "Regen", value = regen, colors = colors, onValueChange = { regen = it })
        Text(
            text = "Drag from either edge to open the left or right panel.",
            color = Color(0xFFB8B9D0),
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun BatteryPage(colors: GliderColors) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        repeat(6) { index ->
            val value = 0.70f + (index * 0.04f)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x1FFFFFFF))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text("Cell ${index + 1}", color = Color.White, modifier = Modifier.width(72.dp))
                LinearProgressIndicator(
                    progress = { value.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    color = colors.gradientStart,
                    trackColor = Color(0x33FFFFFF),
                )
                Text("${3.72f + (index * 0.01f)} V", color = Color.White)
            }
        }
    }
}

@Composable
private fun SliderBlock(
    label: String,
    value: Float,
    colors: GliderColors,
    onValueChange: (Float) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(label, color = Color.White, fontWeight = FontWeight.Bold)
            Text("${value.toInt()}%", color = Color.White)
        }
        GliderSlider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..100f,
            gamma = 1.45f,
            colors = colors,
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0x1FFFFFFF))
            .border(1.dp, Color(0x22FFFFFF), RoundedCornerShape(24.dp))
            .padding(18.dp),
    ) {
        Text(label.uppercase(), color = Color(0xFFB8B9D0), fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = Color.White, fontSize = 44.sp, fontWeight = FontWeight.Black)
            Spacer(Modifier.width(6.dp))
            Text(unit, color = Color(0xFF40E29A), fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun SampleGradientCard(colors: GliderColors) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent,
    ) {
        Box(
            modifier = Modifier
                .height(144.dp)
                .background(
                    Brush.linearGradient(listOf(colors.gradientStart, colors.gradientEnd)),
                )
                .padding(20.dp),
        ) {
            Text(
                text = "Animated tab indicator, sliding panels, and gamma-aware controls.",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                modifier = Modifier.align(Alignment.CenterStart),
            )
        }
    }
}

@Composable
private fun SamplePanel(title: String, color: Color) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .background(Color(0xFF171832))
            .padding(22.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Box(
            modifier = Modifier
                .size(58.dp)
                .background(color, CircleShape),
        )
        Text(title, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
        listOf("Telemetry", "Settings", "Profiles", "Export", "Share").forEach { label ->
            Text(
                text = label,
                color = Color(0xFFE6E4F2),
                fontSize = 18.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x14FFFFFF))
                    .padding(16.dp),
            )
        }
    }
}
