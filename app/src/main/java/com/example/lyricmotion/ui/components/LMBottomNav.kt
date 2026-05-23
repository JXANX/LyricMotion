package com.lyricmotion.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.lyricmotion.Screen
import com.lyricmotion.ui.theme.LMOnSurfaceVariant
import com.lyricmotion.ui.theme.LMPrimary
import com.lyricmotion.ui.theme.LMSurface

@Composable
fun LMBottomNav(navController: NavController) {
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
    NavigationBar(containerColor = LMSurface, tonalElevation = 0.dp) {
        listOf(
            Triple(Icons.Default.Home,           "Inicio",    Screen.Home.route),
            Triple(Icons.Default.BookmarkBorder, "Guardadas", Screen.Saved.route),
            Triple(Icons.Default.Settings,       "Ajustes",   Screen.Settings.route),
        ).forEach { (icon, label, route) ->
            NavigationBarItem(
                selected = currentRoute == route,
                onClick  = {
                    if (currentRoute != route) navController.navigate(route) {
                        popUpTo(Screen.Home.route) { saveState = true }
                        launchSingleTop = true
                        restoreState    = true
                    }
                },
                icon   = { Icon(icon, label) },
                label  = { Text(label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor   = LMPrimary,
                    selectedTextColor   = LMPrimary,
                    unselectedIconColor = LMOnSurfaceVariant,
                    unselectedTextColor = LMOnSurfaceVariant,
                    indicatorColor      = LMPrimary.copy(alpha = 0.15f)
                )
            )
        }
    }
}
