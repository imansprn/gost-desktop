/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 GOST Desktop contributors
 */
package xyz.gobliggg.gost.ui.theme

import androidx.compose.ui.unit.dp

/** 8dp grid spacing tokens (4dp allowed for tight icon–label gaps). */
object Spacing {
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp
    val xl = 24.dp
    val xxl = 32.dp
    val xxxl = 48.dp

    /** Inner padding for dashboard stat cards. */
    val statCardInner = 24.dp

    /** Common dialog interior padding. */
    val dialogPadding = 24.dp

    /** Common empty-state interior padding. */
    val emptyStatePadding = 48.dp

    /** Common table header row vertical padding. */
    val tableHeaderRowV = 16.dp
}
