package com.grid.pos.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel

@Composable
fun UIAlertDialog(
        onDismissRequest: () -> Unit,
        onConfirmation: () -> Unit,
        popupModel: PopupModel
) {
    Dialog(properties = DialogProperties(
        dismissOnBackPress = popupModel.cancelable,
        dismissOnClickOutside = popupModel.cancelable
    ),
        onDismissRequest = { onDismissRequest() }) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(16.dp),
            color = SettingsModel.backgroundColor,
            contentColor = SettingsModel.backgroundColor
        ) {
            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                popupModel.icon?.let { icon ->
                    Spacer(modifier = Modifier.height(10.dp))

                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = icon,
                        contentDescription = "Example Icon",
                        tint = SettingsModel.buttonColor
                    )
                }

                popupModel.dialogTitle?.let { title ->
                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = title,
                        modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally),
                        color = SettingsModel.textColor,
                        style = TextStyle(
                            textDecoration = TextDecoration.None,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        textAlign = TextAlign.Center,
                    )
                }


                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = popupModel.dialogText,
                    modifier = Modifier.fillMaxWidth(),
                    color = SettingsModel.textColor,
                    style = TextStyle(
                        textDecoration = TextDecoration.None,
                        fontWeight = FontWeight.Light,
                        fontSize = 16.sp
                    ),
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                ) {
                    if (!popupModel.negativeBtnText.isNullOrEmpty()) {
                        TextButton(
                            onClick = {
                                onDismissRequest()
                            },
                            modifier = Modifier.weight(1f),
                        ) {
                            Text(
                                popupModel.negativeBtnText!!,
                                color = SettingsModel.textColor,
                                style = TextStyle(
                                    textDecoration = TextDecoration.None,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 16.sp
                                )
                            )
                        }
                    }

                    TextButton(
                        onClick = {
                            onConfirmation()
                        },
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            popupModel.positiveBtnText,
                            color = SettingsModel.textColor,
                            style = TextStyle(
                                textDecoration = TextDecoration.None,
                                fontWeight = FontWeight.Normal,
                                fontSize = 16.sp
                            )
                        )
                    }
                }
            }
        }
    }
}