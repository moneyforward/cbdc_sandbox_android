package com.moneyforward.cbdcsandbox.compose.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyforward.cbdcsandbox.compose.common.CBDCSandboxUserInterface
import com.moneyforward.cbdcsandbox.compose.widget.DigitInputView

data object PasswordInput : BaseScreen("passwordInput") {
    @Composable
    fun Screen(
        // TODO: ViewModel
        // TODO: Events
    ) {
        Content()
    }
}

@Composable
private fun Content() = Column(
    modifier = Modifier.fillMaxWidth()
) {
    Spacer(modifier = Modifier.size(64.dp))
    Text(
        text = "パスワードの入力",
        style = TextStyle(
            fontSize = 24.sp
        ),
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )
    Spacer(modifier = Modifier.size(56.dp))
    DigitInputView()
}

@Preview
@Composable
private fun PreviewPasswordInputScreen() = CBDCSandboxUserInterface {
    Content()
}