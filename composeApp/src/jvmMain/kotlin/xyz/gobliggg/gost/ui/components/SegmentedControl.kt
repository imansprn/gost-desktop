package xyz.gobliggg.gost.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import xyz.gobliggg.gost.ui.theme.GostControlSize
import xyz.gobliggg.gost.ui.theme.GostRadius
import xyz.gobliggg.gost.ui.theme.GostSemantics
import xyz.gobliggg.gost.ui.theme.GostTextStyles
import xyz.gobliggg.gost.ui.theme.Spacing

@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    label: (T) -> String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    equalWidth: Boolean = false,
) {
    val sc = GostSemantics.colors
    Row(
        modifier =
            modifier
                .background(sc.surfaceInput, RoundedCornerShape(GostRadius.md))
                .border(GostControlSize.borderWidth, sc.borderSubtle, RoundedCornerShape(GostRadius.md))
                .padding(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        options.forEach { opt ->
            val isSelected = opt == selected
            val bg = if (isSelected) sc.stateSelected else androidx.compose.ui.graphics.Color.Transparent
            val fg = if (isSelected) sc.textPrimary else sc.textMuted

            val optionModifier =
                if (equalWidth) {
                    Modifier.weight(1f)
                } else {
                    Modifier
                }
            Text(
                text = label(opt),
                color = fg,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                modifier =
                    optionModifier
                        .background(bg, RoundedCornerShape(GostRadius.sm))
                        .clickable(enabled = enabled) { onSelect(opt) }
                        .padding(horizontal = Spacing.sm, vertical = Spacing.sm),
            )
        }
    }
}

@Composable
fun SaaSModeOption(
    label: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sc = GostSemantics.colors
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(GostRadius.md))
                .background(if (selected) sc.stateSelected else androidx.compose.ui.graphics.Color.Transparent)
                .border(
                    GostControlSize.borderWidth,
                    if (selected) sc.statusSuccess.copy(alpha = 0.3f) else androidx.compose.ui.graphics.Color.Transparent,
                    RoundedCornerShape(GostRadius.md),
                )
                .clickable { onClick() }
                .padding(Spacing.sm),
    ) {
        Text(
            label,
            color = if (selected) sc.textPrimary else sc.textSecondary,
            style = GostTextStyles.sectionTitle,
        )
        Text(
            description,
            color = sc.textMuted,
            style = GostTextStyles.rowSubtitle,
        )
    }
}
