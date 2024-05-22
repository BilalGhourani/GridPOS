package com.grid.pos.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import com.grid.pos.model.SettingsModel

@Composable
fun UIAlertDialog(
        onDismissRequest: () -> Unit,
        onConfirmation: () -> Unit,
        dialogTitle: String,
        dialogText: String,
        positiveBtnText: String = "OK",
        negativeBtnText: String = "CANCEL",
        icon: ImageVector,
) {
    AlertDialog(icon = {
        Icon(
            imageVector = icon,
            contentDescription = "Example Icon",
            tint = Color.Red
        )
    },
        title = {
            Text(
                text = dialogTitle,
                color = SettingsModel.textColor,
                style = TextStyle(
                    textDecoration = TextDecoration.None,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                textAlign = TextAlign.Center,
            )
        },
        text = {
            Text(
                text = dialogText,
                color = SettingsModel.textColor,
                style = TextStyle(
                    textDecoration = TextDecoration.None,
                    fontWeight = FontWeight.Light,
                    fontSize = 14.sp
                ),
                textAlign = TextAlign.Center,
            )
        },
        containerColor = SettingsModel.backgroundColor,
        textContentColor = SettingsModel.textColor,
        titleContentColor = SettingsModel.textColor,
        iconContentColor = SettingsModel.textColor,
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirmation()
            }) {
                Text(
                    positiveBtnText,
                    color = SettingsModel.textColor
                )
            }
        },
        dismissButton = {
            TextButton(onClick = {
                onDismissRequest()
            }) {
                Text(
                    negativeBtnText,
                    color = SettingsModel.textColor
                )
            }
        })
}