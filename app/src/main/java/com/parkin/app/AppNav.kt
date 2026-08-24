package com.parkin.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.parkin.app.ui.HomeScreen
import com.parkin.app.ui.LoginScreen
import com.parkin.app.ui.NFCScreen
import com.parkin.app.ui.PaymentScreen
import com.parkin.app.ui.ProfileScreen
import io.github.jan.supabase.auth.auth
import ui.RegisterScreen

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
        supabase.auth.awaitInitialization()

        val session = supabase.auth.currentSessionOrNull()

        startDestination = if (session != null) {
            "home"
        } else {
            "login"
        }
    }

    if (startDestination == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }

        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {

        composable("login") {
            LoginScreen(
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") {
                            inclusive = true
                        }
                    }
                },
                onNavigateToRegister = {
                    navController.navigate("register")
                }
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("home") {
            HomeScreen(
                navController = navController
            )
        }

        composable("nfc") {
            NFCScreen(
                navController = navController
            )
        }

        composable("profile") {
            ProfileScreen(
                navController = navController,
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange
            )

            }
        composable("payment/{amount}") { backStackEntry ->
            val amount = backStackEntry.arguments?.getString("amount")?.toIntOrNull() ?: 0
            PaymentScreen(navController = navController, amountCents = amount)
        }
        }
    }
