package xyz.gobliggg.gost.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import xyz.gobliggg.gost.ui.theme.GostRadius
import xyz.gobliggg.gost.ui.theme.GostSemantics
import xyz.gobliggg.gost.ui.theme.Spacing

enum class BannerType { Info, Success, Warning, Error }

@Composable
fun Banner(
    message: String,
    type: BannerType = BannerType.Info,
    modifier: Modifier = Modifier,
) {
    val sc = GostSemantics.colors

    val (container, foreground, icon) = when (type) {
        BannerType.Info -> Triple(sc.statusInfoContainer, sc.statusInfo, Icons.Default.Info)
        BannerType.Success -> Triple(sc.statusSuccessContainer, sc.statusSuccess, Icons.Default.CheckCircleOutline)
        BannerType.Warning -> Triple(sc.statusWarningContainer, sc.statusWarning, Icons.Default.WarningAmber)
        BannerType.Error -> Triple(sc.statusErrorContainer, sc.statusError, Icons.Default.ErrorOutline)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(container, RoundedCornerShape(GostRadius.sm))
            .border(1.dp, foreground.copy(alpha = 0.25f), RoundedCornerShape(GostRadius.sm))
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = type.name,
                tint = foreground,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(Spacing.sm))
            Text(
                text = message,
                color = sc.textPrimary,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

