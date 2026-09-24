package xyz.gobliggg.gost.ui.theme

import androidx.compose.ui.unit.dp

/**
 * Canonical desktop control dimensions.
 *
 * Keep control sizes separate from layout widths and generic spacing so component
 * semantics do not depend on coincidentally equal numeric values.
 */
object GostControlSize {
    val standardHeight = 40.dp
    val largeHeight = 48.dp

    val iconButtonCompact = 32.dp
    val iconButtonDefault = 36.dp

    val iconSmall = 14.dp
    val icon = 16.dp
    val iconMedium = 18.dp
    val iconLarge = 20.dp
    val emptyStateIcon = 48.dp

    val compactSwitchWidth = 42.dp
    val compactSwitchHeight = 24.dp
    val compactSwitchThumb = 20.dp
    val stepIndicator = 24.dp

    val fieldHorizontalPadding = 12.dp
    val dialogActionMaxWidth = 160.dp
    val borderWidth = 1.dp
}

/** Stable layout dimensions for desktop panes and navigation. */
object GostLayoutSize {
    val sidebarExpanded = 240.dp
    val sidebarCollapsed = 64.dp
    val editorSidebar = 320.dp
    val dialogSplitPane = 300.dp
    val templateListPane = 240.dp
    val compactSelectorPane = 200.dp
}
