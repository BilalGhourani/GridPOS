package com.grid.pos.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.pos.data.DataModel
import com.grid.pos.model.SettingsModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableDropdownMenu(
        modifier: Modifier = Modifier,
        items: MutableList<DataModel> = mutableListOf(),
        label: String = "",
        selectedId: String? = null,
        leadingIcon: @Composable ((Modifier) -> Unit)? = null,
        onLeadingIconClick:()->Unit={},
        onSelectionChange: (DataModel) -> Unit = {},
) {
    var expandedState by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf(label) }
    var selectedItemState by remember { mutableStateOf(label) }
    LaunchedEffect(selectedId) {
        if (!selectedId.isNullOrEmpty()) {
            items.forEach {
                if (it.getId().equals(
                        selectedId,
                        ignoreCase = true
                    )
                ) {
                    selectedItemState = it.getName()
                }
            }
        } else {
            selectedItemState = label
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(color = Color.White)
    ) {
        ExposedDropdownMenuBox(modifier = Modifier
            .fillMaxWidth()
            .background(color = SettingsModel.backgroundColor),
            expanded = expandedState,
            onExpandedChange = {
                expandedState = !expandedState
            }) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .border(
                    1.dp,
                    Color.Black,
                    RoundedCornerShape(10.dp)
                )
                .menuAnchor()
                .clickable {
                    searchText = ""
                }) {
                leadingIcon?.invoke(Modifier
                    .padding(start = 10.dp, top = 10.dp, bottom = 10.dp)
                    .align(Alignment.CenterVertically).clickable { onLeadingIconClick.invoke()
                        expandedState=false }
                )
                Text(
                    modifier = Modifier
                        .padding(start = 10.dp, top = 10.dp, bottom = 10.dp)
                        .align(Alignment.CenterVertically),
                    text = selectedItemState,
                    style = TextStyle(
                        textDecoration = TextDecoration.None,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = Color.Black
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    Icons.Filled.ArrowDropDown,
                    null,
                    Modifier
                        .padding(top = 10.dp, bottom = 10.dp, end = 10.dp)
                        .align(Alignment.CenterVertically)
                        .rotate(if (expandedState) 180f else 0f),
                    tint = Color.Black
                )
            }
            val filteredItems = if (searchText.isEmpty()) items else items.filter {
                it.getName().contains(
                    searchText,
                    ignoreCase = true
                )
            }
            if (filteredItems.isNotEmpty()) {
                DropdownMenu(
                    expanded = expandedState,
                    onDismissRequest = { expandedState = false },
                    modifier = Modifier
                        .exposedDropdownSize()
                        .background(color = Color.White)
                ) {
                    DropdownMenuItem(
                        text = {
                            OutlinedTextField(value = searchText,
                                onValueChange = {
                                    searchText = it
                                },
                                label = {
                                    Text(
                                        "Search",
                                        color = SettingsModel.textColor
                                    )
                                })
                        },
                        onClick = {},
                    )
                    filteredItems.forEach { item ->
                        val text = item.getName()
                        DropdownMenuItem(text = {
                            Text(
                                text = text,
                                color = SettingsModel.textColor
                            )
                        },
                            onClick = {
                                onSelectionChange(item)
                                searchText = text
                                selectedItemState = text
                                expandedState = false
                            })
                    }
                }
            }
        }
    }
}
