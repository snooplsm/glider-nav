package one.adverse.glider

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerScope
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

@Composable
fun GliderPager(
    tabs: List<GliderTab>,
    selectedIndex: Int,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    colors: GliderColors = GliderColors(),
    headerHeight: Dp = 64.dp,
    tabBarVisible: Boolean = true,
    userScrollEnabled: Boolean = true,
    onTabPositions: (selected: TabPosition, all: List<TabPosition>) -> Unit = { _, _ -> },
    tabContent: @Composable (tab: GliderTab, selected: Boolean) -> Unit = { tab, selected ->
        DefaultGliderTab(tab = tab, selected = selected, colors = colors)
    },
    pageContent: @Composable PagerScope.(page: Int) -> Unit,
) {
    require(tabs.isNotEmpty()) { "GliderPager requires at least one tab." }

    val safeSelectedIndex = selectedIndex.coerceIn(0, tabs.lastIndex)
    val pagerState = rememberPagerState(initialPage = safeSelectedIndex) { tabs.size }
    val scope = rememberCoroutineScope()

    LaunchedEffect(safeSelectedIndex, tabs.size) {
        if (pagerState.currentPage != safeSelectedIndex) {
            pagerState.animateScrollToPage(safeSelectedIndex)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                if (page != selectedIndex) {
                    onSelectedIndexChange(page)
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background),
    ) {
        AnimatedVisibility(
            visible = tabBarVisible,
            enter = expandVertically(animationSpec = tween(180), expandFrom = Alignment.Top) +
                slideInVertically(animationSpec = tween(180)) { -it } +
                fadeIn(tween(180)),
            exit = shrinkVertically(animationSpec = tween(140), shrinkTowards = Alignment.Top) +
                slideOutVertically(animationSpec = tween(140)) { -it } +
                fadeOut(tween(140)),
        ) {
            TabRow(
                selectedTabIndex = pagerState.currentPage.coerceIn(0, tabs.lastIndex),
                containerColor = Color.Transparent,
                contentColor = Color.Transparent,
                indicator = { positions ->
                    val current = pagerState.currentPage.coerceIn(0, positions.lastIndex)
                    onTabPositions(positions[current], positions)
                    GliderTabIndicator(
                        tabPositions = positions,
                        selectedIndex = current,
                        colors = colors,
                    )
                },
                divider = { Box {} },
                modifier = Modifier.height(headerHeight),
            ) {
                tabs.forEachIndexed { index, tab ->
                    key(tab.key) {
                        Box(
                            modifier = Modifier
                                .height(headerHeight)
                                .clickable {
                                    onSelectedIndexChange(index)
                                    scope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) {
                            tabContent(tab, pagerState.currentPage == index)
                        }
                    }
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            userScrollEnabled = userScrollEnabled,
            pageContent = pageContent,
        )
    }
}

@Composable
private fun DefaultGliderTab(
    tab: GliderTab,
    selected: Boolean,
    colors: GliderColors,
) {
    Text(
        text = tab.label,
        color = if (selected) colors.selectedContent else colors.unselectedContent,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        modifier = Modifier.padding(horizontal = 8.dp),
    )
}
