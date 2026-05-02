package xyz.gobliggg.gost.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.gobliggg.gost.ui.theme.GostRadius
import xyz.gobliggg.gost.ui.theme.GostSemantics
import xyz.gobliggg.gost.ui.theme.Spacing

@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val sc = GostSemantics.colors
    Row(
        modifier =
            modifier
                .background(sc.surfaceInput, RoundedCornerShape(GostRadius.md))
                .border(1.dp, sc.borderSubtle, RoundedCornerShape(GostRadius.md))
                .padding(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        options.forEach { opt ->
            val isSelected = opt == selected
            val bg = if (isSelected) sc.stateSelected else androidx.compose.ui.graphics.Color.Transparent
            val fg = if (isSelected) sc.textPrimary else sc.textMuted

            Text(
                text = label(opt),
                color = fg,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                modifier =
                    Modifier
                        .background(bg, RoundedCornerShape(GostRadius.sm))
                        .clickable(enabled = enabled) { onSelect(opt) }
                        .padding(horizontal = Spacing.md, vertical = Spacing.sm),
            )
        }
    }
}
