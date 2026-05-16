package xyz.gobliggg.gost.navigation

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NavigationPolicyTest {
    @Test
    fun testNewServiceWizardRoute() {
        val route = newServiceWizardRoute()
        assertTrue(route.startsWith("service-new-"))
    }

    @Test
    fun testNewAutherEditorRoute() {
        val route = newAutherEditorRoute()
        assertTrue(route.startsWith("auther-new-"))
    }

    @Test
    fun testSidebarSelectedRoute() {
        assertEquals("dashboard", sidebarSelectedRoute(listOf("dashboard")))
        assertEquals("services", sidebarSelectedRoute(listOf("services")))
        
        // Service wizard from dashboard
        assertEquals("dashboard", sidebarSelectedRoute(listOf("dashboard", "service-new-123")))
        assertEquals("dashboard", sidebarSelectedRoute(listOf("dashboard", "service-edit:abc")))
        
        // Service wizard from somewhere else (defaults to services)
        assertEquals("services", sidebarSelectedRoute(listOf("settings", "service-new-123")))
        
        // Auther editor
        assertEquals("authers", sidebarSelectedRoute(listOf("dashboard", "auther-new-123")))
        assertEquals("authers", sidebarSelectedRoute(listOf("dashboard", "auther-edit:abc")))
    }
}
