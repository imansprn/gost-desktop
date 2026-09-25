package xyz.gobliggg.gost.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import gost.composeapp.generated.resources.gostLogoPainter
import xyz.gobliggg.gost.ui.theme.GostSemantics
import xyz.gobliggg.gost.ui.theme.Spacing

enum class GostBrandVariant { Color, Monochrome, Muted }

private object GostBrandColors {
    val purple = Color(0xFF8B5CF6)
    val blue = Color(0xFF3B82F6)
    val teal = Color(0xFF14B8A6)
}

@Composable
fun GostBrandLockup(
    modifier: Modifier = Modifier,
    variant: GostBrandVariant = GostBrandVariant.Color,
) {
    val sc = GostSemantics.colors
    val tint = when (variant) {
        GostBrandVariant.Color -> null
        GostBrandVariant.Monochrome -> ColorFilter.tint(sc.textPrimary)
        GostBrandVariant.Muted -> ColorFilter.tint(sc.textMuted)
    }
    Row(modifier, verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        Image(gostLogoPainter(), contentDescription = null,
            colorFilter = tint, modifier = Modifier.size(64.dp))
        Column {
            Text("GOST", color = sc.textPrimary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold)
            Text("TUNNEL FREELY", color = sc.textSecondary,
                style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp)
        }
    }
}

enum class BrandConcept(val label: String, val explanation: String) {
    Tunnel("Tunnel", "Forward traffic through configured services."),
    Routing("Routing", "Choose how traffic passes through proxy chains."),
    Connection("Connection", "Connect listeners to their target endpoints."),
    Privacy("Privacy", "Protection depends on your transport, TLS and authentication settings."),
}

@Composable
fun GostBrandConcepts(modifier: Modifier = Modifier) {
    val sc = GostSemantics.colors
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Spacing.lg)) {
        BrandConcept.entries.forEach { concept ->
            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                verticalAlignment = Alignment.Top) {
                BrandConceptIcon(concept, Modifier.size(28.dp))
                Column(Modifier.weight(1f)) {
                    Text(concept.label, color = sc.textPrimary,
                        style = MaterialTheme.typography.labelLarge)
                    Text(concept.explanation, color = sc.textSecondary,
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun BrandConceptIcon(concept: BrandConcept, modifier: Modifier = Modifier, tint: Color? = null) {
    Canvas(modifier) {
        val unit = size.minDimension / 24f
        val brush = tint?.let { SolidColor(it) } ?: Brush.linearGradient(
            listOf(GostBrandColors.purple, GostBrandColors.blue, GostBrandColors.teal),
            start = Offset(0f, size.height), end = Offset(size.width, 0f))
        val path = Path()
        fun point(x: Float, y: Float) = Offset(x * unit, y * unit)
        when (concept) {
            BrandConcept.Tunnel -> {
                path.moveTo(4 * unit, 20 * unit)
                path.lineTo(4 * unit, 11 * unit)
                path.cubicTo(4 * unit, 0f, 20 * unit, 0f, 20 * unit, 11 * unit)
                path.lineTo(20 * unit, 20 * unit)
                drawPath(path, brush, style = Stroke(3 * unit, cap = StrokeCap.Round))
                drawLine(brush, point(12f, 15f), point(12f, 21f), 3 * unit, StrokeCap.Round)
            }
            BrandConcept.Routing -> {
                path.moveTo(3 * unit, 8 * unit)
                path.lineTo(10 * unit, 8 * unit)
                path.lineTo(15 * unit, 4 * unit)
                path.lineTo(21 * unit, 4 * unit)
                path.moveTo(3 * unit, 20 * unit)
                path.lineTo(9 * unit, 20 * unit)
                path.lineTo(14 * unit, 16 * unit)
                path.lineTo(21 * unit, 16 * unit)
                drawPath(path, brush, style = Stroke(3 * unit, cap = StrokeCap.Round))
                drawCircle(brush, 3 * unit, point(21f, 4f))
                drawCircle(brush, 3 * unit, point(3f, 20f))
            }
            BrandConcept.Connection -> {
                drawLine(brush, point(5f, 19f), point(19f, 5f), 3 * unit, StrokeCap.Round)
                drawCircle(brush, 4 * unit, point(5f, 19f))
                drawCircle(brush, 4 * unit, point(19f, 5f))
            }
            BrandConcept.Privacy -> {
                path.moveTo(12 * unit, 2 * unit)
                path.quadraticTo(7 * unit, 6 * unit, 3 * unit, 6 * unit)
                path.cubicTo(3 * unit, 14 * unit, 5 * unit, 18 * unit, 12 * unit, 22 * unit)
                path.cubicTo(19 * unit, 18 * unit, 21 * unit, 14 * unit, 21 * unit, 6 * unit)
                path.quadraticTo(17 * unit, 6 * unit, 12 * unit, 2 * unit)
                path.close()
                drawPath(path, brush, style = Stroke(2.5f * unit))
            }
        }
    }
}
