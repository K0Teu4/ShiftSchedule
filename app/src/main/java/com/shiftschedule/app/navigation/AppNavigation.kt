package com.shiftschedule.app.navigation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.shiftschedule.app.ui.screens.CalendarScreen
import com.shiftschedule.app.ui.screens.CompareScreen
import com.shiftschedule.app.ui.screens.SettingsScreen
import com.shiftschedule.app.ui.screens.TemplatesScreen
import com.shiftschedule.app.ui.viewmodel.ShiftViewModel
import com.shiftschedule.app.util.tr

sealed class Screen(val route: String, val titleKey: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Calendar : Screen("calendar", "tab_calendar", Icons.Filled.CalendarMonth)
    object Templates : Screen("templates", "tab_templates", Icons.Filled.ViewList)
    object Compare : Screen("compare", "tab_compare", Icons.Filled.CompareArrows)
    object Settings : Screen("settings", "tab_settings", Icons.Filled.Settings)
}

@Composable
fun AppNavigation(viewModel: ShiftViewModel) {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val destination = entry?.destination
    val items = listOf(Screen.Calendar, Screen.Templates, Screen.Compare, Screen.Settings)

    fun navigate(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val tablet = maxWidth >= 600.dp
        if (tablet) {
            Row(Modifier.fillMaxSize()) {
                NavigationRail(containerColor = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxHeight()) {
                    items.forEach { screen ->
                        NavigationRailItem(selected = destination?.hierarchy?.any { it.route == screen.route } == true, onClick = { navigate(screen.route) }, icon = { Icon(screen.icon, tr(screen.titleKey)) }, label = { Text(tr(screen.titleKey)) })
                    }
                }
                NavHost(navController, Screen.Calendar.route, Modifier.fillMaxSize().padding(horizontal = 8.dp)) {
                    composable(Screen.Calendar.route) { CalendarScreen(viewModel) }
                    composable(Screen.Templates.route) { TemplatesScreen(viewModel) }
                    composable(Screen.Compare.route) { CompareScreen(viewModel) }
                    composable(Screen.Settings.route) { SettingsScreen(viewModel) }
                }
            }
        } else {
            Scaffold(
                containerColor = MaterialTheme.colorScheme.background,
                bottomBar = {
                    NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainer, tonalElevation = 0.dp) {
                        items.forEach { screen ->
                            NavigationBarItem(selected = destination?.hierarchy?.any { it.route == screen.route } == true, onClick = { navigate(screen.route) }, icon = { Icon(screen.icon, tr(screen.titleKey)) }, label = { Text(tr(screen.titleKey)) }, alwaysShowLabel = true)
                        }
                    }
                }
            ) { padding ->
                NavHost(navController, Screen.Calendar.route, Modifier.fillMaxSize().padding(padding)) {
                    composable(Screen.Calendar.route) { CalendarScreen(viewModel) }
                    composable(Screen.Templates.route) { TemplatesScreen(viewModel) }
                    composable(Screen.Compare.route) { CompareScreen(viewModel) }
                    composable(Screen.Settings.route) { SettingsScreen(viewModel) }
                }
            }
        }
    }
}
