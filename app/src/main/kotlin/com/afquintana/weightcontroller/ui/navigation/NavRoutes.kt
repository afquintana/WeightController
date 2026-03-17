package com.afquintana.weightcontroller.ui.navigation

sealed class NavRoutes(val route: String) {
    data object Auth : NavRoutes("auth")
    data object Home : NavRoutes("home")
}
