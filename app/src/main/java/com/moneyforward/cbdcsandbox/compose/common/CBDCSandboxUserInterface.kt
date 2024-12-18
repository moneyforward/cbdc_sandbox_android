package com.moneyforward.cbdcsandbox.compose.common

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.moneyforward.cbdcsandbox.ui.theme.CBDCSandboxTheme

@Composable
fun CBDCSandboxUserInterface(
    content: @Composable () -> Unit
) = CBDCSandboxTheme {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        content()
    }
}