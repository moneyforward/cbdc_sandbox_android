package com.moneyforward.cbdcsandbox.ui.widget

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyforward.cbdcsandbox.ui.common.CatalogView

@Composable
fun DigitInputView() {
    val focusRequesters = List(4) { FocusRequester() }
    val digits = remember { mutableStateListOf("", "", "", "") }

    Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
    ) {
        digits.forEachIndexed { index, digit ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 64.dp, height = 80.dp)
                    .border(width = 2.dp, color = Color.Black, shape = RoundedCornerShape(8.dp))
            ) {
                BasicTextField(
                    textStyle = TextStyle(
                        fontSize = 32.sp,
                        textAlign = TextAlign.Center,
                    ),
                    value = digit,
                    onValueChange = { value ->
                        if (value.length <= 1 && value.all { it.isDigit() }) {
                            digits[index] = value
                            if (value.isNotEmpty() && index < 3) {
                                focusRequesters[index + 1].requestFocus()
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    ),
                    visualTransformation = PasswordVisualTransformation(mask = '・'),
                    modifier = Modifier.focusRequester(focusRequesters[index])
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }
}

@Preview
@Composable
private fun PreviewDigitInputView() = CatalogView {
    DigitInputView()
}