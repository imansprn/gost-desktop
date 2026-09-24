package xyz.gobliggg.gost.navigation

import java.util.UUID

/**
 * ## Shell navigation transition model
 *
 * - **REPLACE** — Choosing a sidebar item replaces the entire route stack with that primary route.
 *   Used for: Dashboard, Services, Chains, … (peer destinations).
 * - **PUSH** — Opening the service wizard pushes `service-new-<uuid>` or `service-edit:*` onto the stack
 *   above the current primary route. **Escape** pops one level when depth > 1.
 * - **MODAL / OVERLAY** — Chain create/edit dialog and delete/unsaved-change confirmations.
 *   Dismiss returns to the underlying shell route without changing stack depth.
 * - **RUNTIME SETUP** — An invalid runtime or explicit runtime reconfiguration shows Connection Setup
 *   outside the shell. This local-only app has no authenticated session/401 redirect flow.
 *
 * The app does not use Decompose/Voyager stacks for the shell; behavior is implemented in
 * the root `App` composable with an explicit list of route ids.
 */

/**
 * Stack id for a New Service wizard.
 *
 * A unique id prevents screen-model reuse. Any persisted draft is handled by the wizard itself,
 * where the user can explicitly resume or start fresh.
 */
fun newServiceWizardRoute(): String = "service-new-${UUID.randomUUID()}"

/** Stack id for a **fresh** New Auther editor route. */
fun newAutherEditorRoute(): String = "auther-new-${UUID.randomUUID()}"

/** Sidebar highlight: wizard sits on top of the route it was opened from (dashboard or services). */
fun sidebarSelectedRoute(stack: List<String>): String {
    val top = stack.last()
    val under = stack.getOrNull(stack.size - 2)
    return when {
        top.startsWith("service-new") || top.startsWith("service-edit:") ->
            when (under) {
                "dashboard" -> "dashboard"
                else -> "services"
            }
        top.startsWith("auther-new") || top.startsWith("auther-edit:") -> "authers"
        else -> top
    }
}
