/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 GOST Desktop contributors
 */
package xyz.gobliggg.gost.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Project-specific text roles that are smaller/more opinionated than the default Material3 typography.
 * Keeps all the "micro-type" tuning in one place.
 */
object GostTextStyles {
    val superTitle: TextStyle
        @Composable get() =
            MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )

    val sectionTitle: TextStyle
        @Composable get() =
            MaterialTheme.typography.labelMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
            )

    val tableHeader: TextStyle
        @Composable get() =
            MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp,
            )

    val buttonLabel: TextStyle
        @Composable get() =
            MaterialTheme.typography.labelLarge.copy(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
            )

    val pillLabel: TextStyle
        @Composable get() =
            MaterialTheme.typography.labelSmall.copy(
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
            )

    /** Sidebar navigation item label. */
    val navItem: TextStyle
        @Composable get() =
            MaterialTheme.typography.bodyMedium.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )

    /** Large stat value on dashboard cards. */
    val statValue: TextStyle
        @Composable get() =
            MaterialTheme.typography.headlineMedium.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
            )

    /** Monospace log line text. */
    val logLine: TextStyle
        @Composable get() =
            MaterialTheme.typography.bodySmall.copy(
                fontSize = 11.sp,
            )

    /** Tiny watermark text in sidebar footer. */
    val watermark: TextStyle
        @Composable get() =
            MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.5.sp,
            )
}
