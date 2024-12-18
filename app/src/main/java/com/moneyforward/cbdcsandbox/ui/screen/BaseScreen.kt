package com.moneyforward.cbdcsandbox.ui.screen

import androidx.navigation.NamedNavArgument

sealed class BaseScreen(
    val route: String,
    val navArguments: List<NamedNavArgument> = emptyList()
) {
    data object Home : BaseScreen("home")

}
