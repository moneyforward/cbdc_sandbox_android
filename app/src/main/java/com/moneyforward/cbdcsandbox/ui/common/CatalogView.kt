package com.moneyforward.cbdcsandbox.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import com.moneyforward.cbdcsandbox.ui.theme.CBDCSandboxTheme

@Composable
fun CatalogView(content: @Composable () -> Unit) = CBDCSandboxTheme {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column {
            content()
        }
    }
}