package com.parkin.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.parkin.app.data.supabase
import com.parkin.app.ui.AddVehicleScreen
import com.parkin.app.ui.CarsListScreen
import com.parkin.app.ui.HomeScreen
import com.parkin.app.ui.LoginScreen
import com.parkin.app.ui.NFCScreen
import com.parkin.app.ui.NotificationsScreen
import com.parkin.app.ui.ParkDetailsScreen
import com.parkin.app.ui.ParksScreen
import com.parkin.app.ui.PaymentScreen
import com.parkin.app.ui.ProfileScreen
import com.parkin.app.ui.RegisterScreen
import io.github.jan.supabase.auth.auth
import com.parkin.app.ui.SplashScreen

@Composable
fun AppNavigation(
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val navController = rememberNavController()

    var startDestination by remember {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(Unit) {
        try {
            supabase.auth.awaitInitialization()
            val session = supabase.auth.currentSessionOrNull()

            startDestination = if (session != null) {
                "home"
            } else {
                "login"
            }
        } catch (e: Exception) {
            startDestination = "login"
        }
    }

    if (startDestination == null) {
        SplashScreen()
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable("login") {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate("cars_list") {
                        popUpTo("login") {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate("cars_list") {
                        popUpTo("login") {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("home") {
            HomeScreen(
                navController = navController
            )
        }

        composable("nfc") {
            NFCScreen(navController = navController)
        }

        composable("profile") {
            ProfileScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange
            )
        }

        composable("payment") {
            PaymentScreen(navController = navController)
        }


        composable("parks") {
            ParksScreen(
                navController = navController,
                isDarkTheme = isDarkTheme
            )
        }

        composable("park/{parkId}") { backStackEntry ->
            val parkId = backStackEntry.arguments?.getString("parkId") ?: ""

            ParkDetailsScreen(
                navController = navController,
                parkId = parkId
            )
        }

        composable("cars_list") {
            CarsListScreen(
                navController = navController
            )
        }

        composable("add_vehicle") {
            AddVehicleScreen(
                navController = navController
            )
        }

        composable("notifications") {
            NotificationsScreen(navController = navController)
        }
    }
}