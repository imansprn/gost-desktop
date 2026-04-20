package xyz.gobliggg.gost.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.drawBehind
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import xyz.gobliggg.gost.ui.theme.*
import xyz.gobliggg.gost.data.AppState
import xyz.gobliggg.gost.ui.ShellFeedback

/**
 * Main app shell with sidebar + content area.
 * Used for all screens except Connection Setup (S-01).
 */
@Composable
fun AppShell(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onDisconnect: () -> Unit,
    content: @Composable () -> Unit,
) {
    val isRuntimeValid by AppState.isRuntimeValid.collectAsState()
    val settings by AppState.settings.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        ShellFeedback.snackbars.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    val sidebarItems = remember {
        listOf(
            SidebarItem("dashboard", "Dashboard", Icons.Default.BarChart),
            SidebarItem("services", "Tunnels", Icons.Default.Router),
            SidebarItem("chains", "Chains", Icons.Default.Link),
            SidebarItem("authers", "Authers", Icons.Default.Shield),
            SidebarItem("advanced", "Advanced", Icons.Default.Tune),
            SidebarItem("logs", "Logs", Icons.Default.Terminal),
            SidebarItem("config", "Config", Icons.Default.Code),
            SidebarItem("settings", "Settings", Icons.Default.Settings),
        )
    }

    SaaSAppBackground {
        Row(modifier = Modifier.fillMaxSize()) {
            Sidebar(
                items = sidebarItems,
                selectedId = currentRoute,
                collapsed = settings.sidebarCollapsed,
                connectionName = "Local Mode",
                isRuntimeValid = isRuntimeValid,
                gostVersion = null,
                onItemSelected = onNavigate,
                onToggleCollapse = {
                    AppState.updateSettings { it.copy(sidebarCollapsed = !it.sidebarCollapsed) }
                },
                onDisconnect = onDisconnect,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                ) {
                    content()
                    SnackbarHost(
                        hostState = snackbarHostState,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(Spacing.lg),
                    )
                }
            }
        }
    }
}
