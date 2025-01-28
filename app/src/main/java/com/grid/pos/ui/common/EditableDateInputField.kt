package com.grid.pos.ui.common

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import com.grid.pos.model.SettingsModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun EditableDateInputField(
    modifier: Modifier,
    label: String,
    date: String,
    dateTimeFormat: String = "yyyy-MM-dd HH:mm:ss.SSS",
    is24Hour: Boolean = false,
    onFocusChanged: ((FocusState) -> Unit)? = null,
    focusRequester: FocusRequester = FocusRequester(),
    imeAction: ImeAction = ImeAction.Next,
    onAction: KeyboardActionScope.() -> Unit = {},
    onDateTimeChange: (String) -> Unit
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    try {
        if (date.isNotBlank()) {
            val simpleDateFormat = SimpleDateFormat(dateTimeFormat, Locale.getDefault())
            calendar.time = simpleDateFormat.parse(date) ?: Calendar.getInstance().time
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    // Generate regex from the format
    val regex = dateTimeFormat
        .replace("yyyy", "\\d{4}")
        .replace("MM", "\\d{2}")
        .replace("dd", "\\d{2}")
        .replace("HH", "\\d{2}")
        .replace("mm", "\\d{2}")
        .replace("ss", "\\d{2}")
        .replace("SSS", "\\d{3}")

    fun showDateTimePicker() {
        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                // After date is selected, show time picker
                TimePickerDialog(
                    context,
                    { _, selectedHour, selectedMinute ->
                        // Update seconds and milliseconds to defaults
                        val formattedDateTime = try {
                            val simpleDateFormat = SimpleDateFormat(
                                dateTimeFormat,
                                Locale.getDefault()
                            )
                            val selectedCalendar = Calendar.getInstance().apply {
                                set(
                                    selectedYear,
                                    selectedMonth,
                                    selectedDay,
                                    selectedHour,
                                    selectedMinute,
                                    0
                                )
                            }
                            simpleDateFormat.format(selectedCalendar.time)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            ""
                        }
                        onDateTimeChange(formattedDateTime)
                    },
                    hour,
                    minute,
                    is24Hour
                ).show()
            },
            year,
            month,
            day
        ).show()
    }

    Box(
        modifier = modifier
            .height(80.dp),
    ) {
        OutlinedTextField(
            value = date,
            onValueChange = { newDateTime ->
                // Validate input based on the generated regex
                if (newDateTime.matches(Regex("^$regex\$"))) {
                    onDateTimeChange(newDateTime)
                }
            },
            shape = RoundedCornerShape(15.dp),
            label = { Text(text = label, color = SettingsModel.textColor) },
            readOnly = true,
            enabled = false,
            singleLine = true,
            maxLines = 1,
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .onFocusChanged { onFocusChanged?.invoke(it) }
                .clickable {
                    showDateTimePicker()
                },
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
            textStyle = TextStyle( // Ensure text is vertically centered
                textAlign = TextAlign.Start,
                fontSize = 16.sp
            ),
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Select Date and Time",
                    tint = Color.Black,
                    modifier = Modifier.clickable {
                        showDateTimePicker()
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
