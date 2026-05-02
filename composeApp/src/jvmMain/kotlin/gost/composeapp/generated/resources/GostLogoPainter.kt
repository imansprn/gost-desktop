/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 GOST Desktop contributors
 *
 * Bridges internal compose-resource accessors (same package) for use from app code.
 */
package gost.composeapp.generated.resources

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.imageResource

/** App logo from composeResources (backed by drawable/gost.png). */
@Composable
fun gostLogoPainter(): Painter {
    val image = imageResource(Res.drawable.gost)
    return remember(image) {
        BitmapPainter(image, filterQuality = FilterQuality.High)
    }
}
