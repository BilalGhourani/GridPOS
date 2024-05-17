package com.grid.pos.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
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
            contentDescription = "Example Icon"
        )
    },
        title = {
            Text(
                text = dialogTitle,
                color = SettingsModel.textColor
            )
        },
        text = {
            Text(
                text = dialogText,
                color = SettingsModel.textColor
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