/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright 2026 GOST Desktop contributors
 */
package xyz.gobliggg.gost

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import xyz.gobliggg.gost.data.AppState
import xyz.gobliggg.gost.navigation.newAutherEditorRoute
import xyz.gobliggg.gost.navigation.newServiceWizardRoute
import xyz.gobliggg.gost.navigation.sidebarSelectedRoute
import xyz.gobliggg.gost.screen.advanced.AdvancedScreen
import xyz.gobliggg.gost.screen.authers.AutherFormScreen
import xyz.gobliggg.gost.screen.authers.AuthersScreen
import xyz.gobliggg.gost.screen.chains.ChainsScreen
import xyz.gobliggg.gost.screen.config.ConfigEditorScreen
import xyz.gobliggg.gost.screen.connection.ConnectionScreen
import xyz.gobliggg.gost.screen.dashboard.DashboardScreen
import xyz.gobliggg.gost.screen.logs.LogsScreen
import xyz.gobliggg.gost.screen.serviceform.ServiceFormScreen
import xyz.gobliggg.gost.screen.services.ServicesScreen
import xyz.gobliggg.gost.screen.settings.SettingsScreen
import xyz.gobliggg.gost.ui.GlobalWindowShortcuts
import xyz.gobliggg.gost.ui.WindowTitleState
import xyz.gobliggg.gost.ui.components.AppShell
import xyz.gobliggg.gost.ui.theme.GostTheme

@Composable
fun App() {
    LaunchedEffect(Unit) {
        AppState.initialize()
    }

    val isInitialized by AppState.isInitialized.collectAsState()
    if (!isInitialized) return

    val isRuntimeValid by AppState.isRuntimeValid.collectAsState()

    LaunchedEffect(isRuntimeValid) {
        WindowTitleState.update(isRuntimeValid, "Local Process Managed")
    }

    GostTheme(darkTheme = true) {
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
        ) {
            if (!isRuntimeValid) {
                ConnectionScreen(onConnected = { }).Content()
            } else {
                MainAppContent()
            }
        }
    }
}

@Composable
private fun MainAppContent() {
    val initialRoute = remember { AppState.takePendingShellRoute() ?: "dashboard" }
    var stack by remember { mutableStateOf(listOf(initialRoute)) }
    val topRoute = stack.last()
    val sidebarRoute = sidebarSelectedRoute(stack)

    val popWizardOrService: () -> Unit = {
        stack = if (stack.size > 1) stack.dropLast(1) else listOf("services")
    }

    val popEditorOrAuthers: () -> Unit = {
        stack = if (stack.size > 1) stack.dropLast(1) else listOf("authers")
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    val primaryMod = event.isCtrlPressed || event.isMetaPressed
                    if (primaryMod) {
                        when (event.key) {
                            Key.S -> {
                                val h = GlobalWindowShortcuts.saveHandler
                                if (h != null) {
                                    h()
                                    true
                                } else {
                                    false
                                }
                            }
                            Key.R -> {
                                val h = GlobalWindowShortcuts.refreshHandler
                                if (h != null) {
                                    h()
                                    true
                                } else {
                                    false
                                }
                            }
                            Key.Comma -> {
                                stack = listOf("settings")
                                true
                            }
                            else -> false
                        }
                    } else if (event.key == Key.Escape && stack.size > 1) {
                        stack = stack.dropLast(1)
                        true
                    } else {
                        false
                    }
                },
    ) {
        AppShell(
            currentRoute = sidebarRoute,
            onNavigate = { route -> stack = listOf(route) },
            onDisconnect = { AppState.disconnect() },
        ) {
            when {
                topRoute == "dashboard" ->
                    DashboardScreen(
                        onCreateService = { stack = listOf("dashboard", newServiceWizardRoute()) },
                    ).Content()

                topRoute == "services" ->
                    ServicesScreen(
                        onCreateService = { stack = listOf("services", newServiceWizardRoute()) },
                        onEditService = { name -> stack = listOf("services", "service-edit:$name") },
                    ).Content()

                topRoute.startsWith("service-new") ->
                    key(topRoute) {
                        ServiceFormScreen(
                            routeId = topRoute,
                            editName = null,
                            onDone = popWizardOrService,
                            onCancel = popWizardOrService,
                        ).Content()
                    }

                topRoute == "chains" ->
                    ChainsScreen(
                        onEditService = { svc -> stack = listOf("services", "service-edit:$svc") },
                    ).Content()

                topRoute == "authers" ->
                    AuthersScreen(
                        onCreateAuther = { stack = listOf("authers", newAutherEditorRoute()) },
                        onEditAuther = { name -> stack = listOf("authers", "auther-edit:$name") },
                    ).Content()
                topRoute == "advanced" -> AdvancedScreen().Content()
                topRoute == "logs" -> LogsScreen().Content()
                topRoute == "config" -> ConfigEditorScreen().Content()
                topRoute == "settings" -> SettingsScreen().Content()

                topRoute.startsWith("auther-new") ->
                    key(topRoute) {
                        AutherFormScreen(
                            routeId = topRoute,
                            editName = null,
                            onDone = popEditorOrAuthers,
                            onCancel = popEditorOrAuthers,
                        ).Content()
                    }

                topRoute.startsWith("service-edit:") -> {
                    val name = topRoute.removePrefix("service-edit:")
                    key(topRoute) {
                        ServiceFormScreen(
                            routeId = topRoute,
                            editName = name,
                            onDone = popWizardOrService,
                            onCancel = popWizardOrService,
                        ).Content()
                    }
                }

                topRoute.startsWith("auther-edit:") -> {
                    val name = topRoute.removePrefix("auther-edit:")
                    key(topRoute) {
                        AutherFormScreen(
                            routeId = topRoute,
                            editName = name,
                            onDone = popEditorOrAuthers,
                            onCancel = popEditorOrAuthers,
                        ).Content()
                    }
                }

                else ->
                    DashboardScreen(
                        onCreateService = { stack = listOf("dashboard", newServiceWizardRoute()) },
                    ).Content()
            }
        }
    }
}
