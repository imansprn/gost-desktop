/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 GOST Manager contributors
 */
package xyz.gobliggg.gost.ui

import java.awt.Window
import javax.swing.SwingUtilities

/**
 * On macOS, use a transparent unified title bar so the Compose layer (same color as the app body)
 * shows through the title bar region instead of the default gray chrome.
 */
fun applyUnifiedTitleBarIfSupported(window: Window) {
    if (!System.getProperty("os.name").orEmpty().lowercase().contains("mac")) return
    SwingUtilities.invokeLater {
        val root = SwingUtilities.getRootPane(window) ?: return@invokeLater
        root.putClientProperty("apple.awt.fullWindowContent", true)
        root.putClientProperty("apple.awt.transparentTitleBar", true)
    }
}
