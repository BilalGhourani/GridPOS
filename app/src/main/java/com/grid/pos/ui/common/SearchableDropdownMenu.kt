package com.grid.pos.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.toSize
import com.grid.pos.data.DataModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableDropdownMenu(
    modifier: Modifier = Modifier,
    items: MutableList<DataModel> = mutableListOf(),
    label: String = "",
    selectedId: String? = null,
    onSelectionChange: (DataModel) -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf(label) }
    var selectedItemState by remember { mutableStateOf(label) }
    LaunchedEffect(selectedId) {
        if (!selectedId.isNullOrEmpty()) {
            items.forEach {
                if (it.getId().equals(selectedId, ignoreCase = true)) {
                    selectedItemState = it.getName()
                }
            }
        } else {
            selectedItemState = label
        }
    }

    Box(modifier = modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(
            modifier = Modifier.fillMaxWidth(),
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                value = selectedItemState,
                onValueChange = { },
                readOnly = true,
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clickable {
                        searchText = ""
                    },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                }
            )
            val filteredItems = if (searchText.isEmpty()) items else
                items.filter { it.getName().contains(searchText, ignoreCase = true) }
            if (filteredItems.isNotEmpty()) {
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.exposedDropdownSize()
                ) {
                    DropdownMenuItem(
                        text = {
                            OutlinedTextField(
                                value = searchText,
                                onValueChange = {
                                    searchText = it
                                },
                                label = {
                                    Text(
                                        "Search for an User",
                                        color = SettingsModel.textColor
                                    )
                                }
                            )
                        },
                        onClick = {},
                    )
                    filteredItems.forEach { item ->
                        val text = item.getName()
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = text,
                                    color = SettingsModel.textColor
                                )
                            },
                            onClick = {
                                onSelectionChange(item)
                                searchText = text
                                selectedItemState = text
                                expanded = false
                            })
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchableDropdownMenuPreview() {
    GridPOSTheme {
        SearchableDropdownMenu(
            items = Utils.users
        )
    }
}
