package com.afquintana.weightcontroller.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.afquintana.weightcontroller.ui.screen.auth.AuthScreen
import com.afquintana.weightcontroller.ui.screen.home.HomeScreen
import com.afquintana.weightcontroller.viewmodel.AuthViewModel
import com.afquintana.weightcontroller.viewmodel.HomeViewModel

@Composable
fun WeightControllerNavHost() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = if (authState.isLoggedIn) NavRoutes.Home.route else NavRoutes.Auth.route
    ) {
        composable(NavRoutes.Auth.route) {
            AuthScreen(
                state = authState,
                onEmailChange = authViewModel::onEmailChange,
                onPasswordChange = authViewModel::onPasswordChange,
                onNameChange = authViewModel::onNameChange,
                onHeightChange = authViewModel::onHeightChange,
                onIdealWeightChange = authViewModel::onIdealWeightChange,
                onSexChange = authViewModel::onSexChange,
                onToggleMode = authViewModel::toggleMode,
                onLogin = {
                    authViewModel.login {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Auth.route) { inclusive = true }
                        }
                    }
                },
                onRegister = {
                    authViewModel.register {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Auth.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(NavRoutes.Home.route) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val homeState by homeViewModel.uiState.collectAsState()
            HomeScreen(
                state = homeState,
                onWeightInputChange = homeViewModel::onWeightInputChange,
                onAddWeight = homeViewModel::addWeight,
                onDeleteWeight = homeViewModel::deleteWeight,
                onLogout = {
                    homeViewModel.logout()
                    navController.navigate(NavRoutes.Auth.route) {
                        popUpTo(NavRoutes.Home.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
