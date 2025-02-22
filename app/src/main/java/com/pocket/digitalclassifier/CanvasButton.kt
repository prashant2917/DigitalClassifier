package com.pocket.digitalclassifier

import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun CanvasButton(buttonText: String, onClick: () -> Unit) {
    Button(
        onClick = onClick
    ) {
        Text(buttonText)
    }

}