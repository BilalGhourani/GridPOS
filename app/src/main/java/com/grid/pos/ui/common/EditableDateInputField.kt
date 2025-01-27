package com.grid.pos.ui.common

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.grid.pos.model.SettingsModel
import java.util.Calendar

@Composable
fun EditableDateInputField(
    modifier: Modifier,
    label: String,
    date: String,
    onFocusChanged: ((FocusState) -> Unit)? = null,
    focusRequester: FocusRequester = FocusRequester(),
    imeAction: ImeAction = ImeAction.Next,
    onAction: KeyboardActionScope.() -> Unit = {},
    onDateTimeChange: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        OutlinedTextField(
            value = date,
            onValueChange = { newDateTime ->
                // Validate input for format "YYYY-MM-DD HH:mm:ss.SSS"
                if (newDateTime.matches(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}"))) {
                    onDateTimeChange(newDateTime)
                }
            },
            shape = RoundedCornerShape(15.dp),
            label = { Text(text = label, color = SettingsModel.textColor) },
            readOnly = true,
            enabled = false,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged { onFocusChanged?.invoke(it) },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Number,
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
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date",
                    tint = Color.Black,
                    modifier = Modifier.clickable {
                        DatePickerDialog(
                            context,
                            { _, selectedYear, selectedMonth, selectedDay ->
                                // After date is selected, show time picker
                                TimePickerDialog(
                                    context,
                                    { _, selectedHour, selectedMinute ->
                                        // Update seconds and milliseconds to defaults
                                        val formattedDateTime = String.format(
                                            "%04d-%02d-%02d %02d:%02d:%02d.%03d",
                                            selectedYear,
                                            selectedMonth + 1,
                                            selectedDay,
                                            selectedHour,
                                            selectedMinute,
                                            0, // Default seconds
                                            0 // Default milliseconds
                                        )
                                        onDateTimeChange(formattedDateTime)
                                    },
                                    hour,
                                    minute,
                                    true // 24-hour format
                                ).show()
                            },
                            year,
                            month,
                            day
                        ).show()
                    }
                )
            },
            placeholder = { Text("DD/MM/YYYY") },
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Black,
                focusedBorderColor = SettingsModel.buttonColor,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                cursorColor = SettingsModel.buttonColor,
                disabledTextColor = Color.Black,
                disabledBorderColor = SettingsModel.buttonColor,
            )
        )
    }
}
