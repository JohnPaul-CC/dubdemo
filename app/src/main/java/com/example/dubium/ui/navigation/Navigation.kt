package com.example.dubium.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.dubium.ui.screens.LoginScreen
import com.example.dubium.ui.screens.RegisterScreen
import com.example.dubium.ui.screens.ProfileScreen

// Rutas de navegación
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home/{username}"

    fun createHomeRoute(username: String) = "home/$username"
}

@Composable
fun DubiumNavigation(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // Pantalla de Login
        composable(Routes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER)
                },
                onNavigateToHome = { username ->
                    navController.navigate(Routes.createHomeRoute(username)) {
                        // Limpiar el stack de navegación para que no pueda volver atrás
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla de Registro
        composable(Routes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.popBackStack()
                },
                onNavigateToHome = { username ->
                    navController.navigate(Routes.createHomeRoute(username)) {
                        // Limpiar el stack de navegación
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        // Pantalla de Home
        composable(Routes.HOME) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: "Usuario"
            ProfileScreen(username = username)
        }
    }
}