/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 GOST Manager contributors
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
        @Composable get() = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
        )

    val tableHeader: TextStyle
        @Composable get() = MaterialTheme.typography.labelSmall.copy(
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.5.sp,
        )

    val buttonLabel: TextStyle
        @Composable get() = MaterialTheme.typography.labelLarge.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
        )

    val pillLabel: TextStyle
        @Composable get() = MaterialTheme.typography.labelSmall.copy(
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
        )
}

