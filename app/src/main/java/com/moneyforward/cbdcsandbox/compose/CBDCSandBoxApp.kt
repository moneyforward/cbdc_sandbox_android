package com.moneyforward.cbdcsandbox.compose

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.moneyforward.cbdcsandbox.ui.screen.CardReader
import com.moneyforward.cbdcsandbox.ui.screen.PasswordInput

@Composable
fun CBDCSandBoxApp() {
    val navController = rememberNavController()
    CBDCSandBoxNavHost(
        navController = navController
    )
}

@Composable
fun CBDCSandBoxNavHost(
    navController: NavHostController
) {
    NavHost(navController = navController, startDestination = PasswordInput.route) {
        composable(route = PasswordInput.route) {
            PasswordInput.Screen()
        }
        composable(route = CardReader.route) {
            CardReader.Screen()
        }
    }
}