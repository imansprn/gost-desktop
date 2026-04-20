package xyz.gobliggg.gost.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import kotlin.random.Random

/**
 * Adds a subtle grain texture to any background.
 */
fun Modifier.grain(alpha: Float = 0.05f): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()
        
        // Simple pixel-based grain for desktop
        val density = 2f
        val horizontalDots = (size.width / density).toInt()
        val verticalDots = (size.height / density).toInt()
        
        for (x in 0 until horizontalDots step 2) {
            for (y in 0 until verticalDots step 2) {
                if (Random.nextFloat() > 0.8f) {
                    drawRect(
                        color = Color.White.copy(alpha = alpha * Random.nextFloat()),
                        topLeft = Offset(x * density, y * density),
                        size = Size(density, density)
                    )
                }
            }
        }
    }
)

/**
 * Glassmorphism effect with border.
 */
fun Modifier.glass(
    color: Color = GlassWhite,
    borderColor: Color = GlassBorder,
    cornerRadius: Float = 16f
): Modifier = this.then(
    Modifier
        .background(color, RoundedCornerShape(cornerRadius.dp))
        .border(1.dp, borderColor, RoundedCornerShape(cornerRadius.dp))
)

/**
 * Adds a subtle grid background.
 */
fun Modifier.gridPattern(
    color: Color = Color.White.copy(alpha = 0.03f),
    gridSize: Float = 40f
): Modifier = this.then(
    Modifier.drawWithContent {
        drawContent()
        
        val w = size.width
        val h = size.height
        
        // Vertical lines
        var x = 0f
        while (x < w) {
            drawLine(
                color = color,
                start = Offset(x, 0f),
                end = Offset(x, h),
                strokeWidth = 1f
            )
            x += gridSize
        }
        
        // Horizontal lines
        var y = 0f
        while (y < h) {
            drawLine(
                color = color,
                start = Offset(0f, y),
                end = Offset(w, y),
                strokeWidth = 1f
            )
            y += gridSize
        }
    }
)
