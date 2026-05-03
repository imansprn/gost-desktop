/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 GOST Desktop contributors
 */
package xyz.gobliggg.gost.ui.theme

import androidx.compose.ui.unit.dp

/** Shared corner radii — all values on 4pt sub-grid. */
object GostRadius {
    /** Ultra-small (checkboxes, tags). */
    val xs = 4.dp

    /** Small controls (chips, compact rows). */
    val sm = 8.dp

    /** Standard controls (fields, buttons). */
    val md = 12.dp

    /** Panels / cards. */
    val lg = 16.dp

    /** Full-round pill shapes. */
    val pill = 100.dp

    /** Perfect round for icons. */
    val round = 999.dp
}
