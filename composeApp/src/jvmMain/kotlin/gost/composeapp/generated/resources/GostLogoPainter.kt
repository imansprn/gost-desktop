/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 GOST Manager contributors
 *
 * Bridges internal compose-resource accessors (same package) for use from app code.
 */
package gost.composeapp.generated.resources

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import org.jetbrains.compose.resources.painterResource

/** App logo from composeResources (backed by drawable/gost.png). */
@Composable
fun gostLogoPainter(): Painter = painterResource(Res.drawable.gost)
