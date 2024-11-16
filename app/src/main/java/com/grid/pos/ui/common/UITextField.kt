package com.grid.pos.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.FocusState
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import com.grid.pos.model.SettingsModel

@Composable
fun UITextField(
        modifier: Modifier = Modifier,
        defaultValue: String = "",
        label: String? = null,
        placeHolder: String? = null,
        capitalization: KeyboardCapitalization = KeyboardCapitalization.None,
        autoCorrect: Boolean = true,
        onFocusChanged: ((FocusState) -> Unit)? = null,
        focusRequester: FocusRequester = FocusRequester(),
        keyboardType: KeyboardType = KeyboardType.Text,
        imeAction: ImeAction = ImeAction.Next,
        onAction: KeyboardActionScope.() -> Unit = {},
        visualTransformation: VisualTransformation = VisualTransformation.None,
        maxLines: Int = 1,
        readOnly: Boolean = false,
        enabled: Boolean = true,
        leadingIcon: @Composable (() -> Unit)? = null,
        trailingIcon: @Composable (() -> Unit)? = null,
        onValueChange: (String) -> Unit
) {
    Box(
        modifier = modifier.height(
            max(
                80.dp,
                (maxLines * 40).dp
            )
        )
    ) {

        OutlinedTextField(
            value = defaultValue,
            onValueChange = {
                onValueChange(it)
            },
            shape = RoundedCornerShape(15.dp),
            label = {
                label?.let {
                    Text(
                        text = label,
                        color = SettingsModel.textColor
                    )
                }.run { null }
            },
            readOnly = readOnly,
            enabled = enabled,
            placeholder = { placeHolder?.let { Text(text = placeHolder) }.run { null } },
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .onFocusChanged { onFocusChanged?.invoke(it) },
            keyboardOptions = KeyboardOptions(
                capitalization = capitalization,
                autoCorrect = autoCorrect,
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    onAction.invoke(this)
                },
                onDone = {
                    onAction.invoke(this)
                },
            ),
            maxLines = maxLines,
            singleLine = maxLines == 1,
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            visualTransformation = visualTransformation,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Black,
                focusedBorderColor = SettingsModel.buttonColor,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = SettingsModel.buttonColor
            )
        )
    }
}