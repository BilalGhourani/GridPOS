package com.grid.pos.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.grid.pos.R
import com.grid.pos.data.EntityModel
import com.grid.pos.model.SettingsModel

@Composable
fun SearchableDropdownMenuEx(
    modifier: Modifier = Modifier,
    items: MutableList<EntityModel> = mutableListOf(),
    paddingValues: PaddingValues = PaddingValues(0.dp),
    placeholder: String? = null,
    label: String = "",
    searchEnteredText: String? = null,
    selectedId: String? = null,
    showSelected: Boolean = true,
    enableSearch: Boolean = true,
    collapseOnInit: Boolean = false,
    color: Color = SettingsModel.backgroundColor,
    leadingIcon: @Composable ((Modifier) -> Unit)? = null,
    searchLeadingIcon: @Composable (() -> Unit)? = null,
    cornerRadius: Dp = 15.dp,
    height: Dp = 70.dp,
    minHeight: Dp = 50.dp,// 1 row as minimum
    maxHeight: Dp = 170.dp,// 4 rows as maximum
    onLeadingIconClick: () -> Unit = {},
    onLoadItems: () -> Unit = {},
    onNoSearchResultsFound: ((String) -> Unit)? = null,
    onSelectionChange: (EntityModel) -> Unit = {},
) {
    var isLoaded by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }
    var selectedItemState by remember { mutableStateOf(label) }
    LaunchedEffect(
        selectedId,
        items
    ) {
        isLoaded = items.isNotEmpty()
        if (showSelected && !selectedId.isNullOrEmpty()) {
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

    LaunchedEffect(key1 = searchEnteredText) {
        if (!searchEnteredText.isNullOrEmpty()) {
            searchText = searchEnteredText
        }
    }

    // Handle initialization of collapse state
    LaunchedEffect(collapseOnInit) {
        if (collapseOnInit) {
            isExpanded = false
        }
    }

    Box(modifier = modifier
        .fillMaxWidth()
        .padding(paddingValues)
        .pointerInput(isExpanded) {
            detectTapGestures(onTap = {
                isExpanded = false
            })
        }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = color)
        ) {
            if (!placeholder.isNullOrEmpty()) {
                // If text is present, show placeholder at the top
                Text(
                    text = placeholder,
                    color = SettingsModel.textColor,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(
                        start = 8.dp,
                        bottom = 4.dp
                    )
                )
            }
            Row(modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .border(
                    1.dp,
                    Color.Black,
                    RoundedCornerShape(cornerRadius)
                )
                .clickable {
                    if (!isExpanded && !isLoaded) {
                        isLoaded = true
                        onLoadItems.invoke()
                    }
                    isExpanded = !isExpanded
                }) {
                leadingIcon?.invoke(
                    Modifier
                        .padding(
                            start = 10.dp,
                            top = 10.dp,
                            bottom = 10.dp
                        )
                        .align(Alignment.CenterVertically)
                        .clickable {
                            onLeadingIconClick.invoke()
                            isExpanded = false
                        })
                Text(
                    modifier = Modifier
                        .padding(
                            start = 10.dp,
                            top = 10.dp,
                            bottom = 10.dp
                        )
                        .align(Alignment.CenterVertically),
                    text = selectedItemState,
                    style = TextStyle(
                        textDecoration = TextDecoration.None,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = SettingsModel.textColor
                )

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    Icons.Filled.ArrowDropDown,
                    null,
                    Modifier
                        .padding(
                            top = 10.dp,
                            bottom = 10.dp,
                            end = 10.dp
                        )
                        .align(Alignment.CenterVertically)
                        .rotate(if (isExpanded) 180f else 0f),
                    tint = SettingsModel.buttonColor
                )
            }

            if (isExpanded) {
                val filteredItems = if (searchText.isEmpty()) items else items.filter {
                    it.search(searchText)
                }
                Popup(
                    onDismissRequest = { isExpanded = false },
                    properties = PopupProperties(
                        focusable = true, // Close on outside tap
                        dismissOnBackPress = true, // Close on back press
                        dismissOnClickOutside = true // Close on outside click
                    )
                ) {
                    Surface(
                        modifier = Modifier.padding(top = height + 2.dp, start = 5.dp, end = 5.dp)
                            .clickable { isExpanded = false },
                        tonalElevation = 5.dp,
                        shadowElevation = 5.dp,
                        color = color,
                        contentColor = color,
                        shape = RoundedCornerShape(cornerRadius),
                        border = BorderStroke(
                            1.dp,
                            Color.Black
                        )
                    ) {
                        Column {
                            if (enableSearch) {
                                OutlinedTextField(modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        horizontal = 8.dp,
                                        vertical = 5.dp
                                    ),
                                    value = searchText,
                                    onValueChange = {
                                        searchText = it
                                    },
                                    shape = RoundedCornerShape(cornerRadius),
                                    label = {
                                        Text(
                                            "Search",
                                            color = SettingsModel.textColor
                                        )
                                    },
                                    keyboardOptions = KeyboardOptions(
                                        capitalization = KeyboardCapitalization.None,
                                        autoCorrectEnabled = true,
                                        keyboardType = KeyboardType.Text,
                                        imeAction = ImeAction.Search
                                    ),
                                    keyboardActions = KeyboardActions(
                                        onSearch = {
                                            if (searchText.isNotEmpty() && filteredItems.isEmpty()) {
                                                onNoSearchResultsFound?.invoke(searchText)
                                            }

                                        }
                                    ),
                                    leadingIcon = searchLeadingIcon,
                                    trailingIcon = {
                                        IconButton(onClick = {
                                            searchText = ""
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Clear,
                                                contentDescription = "clear",
                                                tint = SettingsModel.buttonColor
                                            )
                                        }
                                    })
                            }

                            if (filteredItems.isNotEmpty()) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                        .padding(top = 10.dp)
                                        .heightIn(
                                            min = minHeight,
                                            max = maxHeight
                                        ),
                                    contentPadding = PaddingValues(horizontal = 10.dp)
                                ) {
                                    filteredItems.forEach { dataObj ->
                                        item {
                                            Text(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(40.dp)
                                                    .clickable {
                                                        onSelectionChange(dataObj)
                                                        if (showSelected) selectedItemState =
                                                            dataObj.getName()
                                                        isExpanded = false
                                                    },
                                                text = dataObj.getName(),
                                                maxLines = 2,
                                                color = SettingsModel.textColor,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            } else {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .background(color = color),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Image(
                                        modifier = Modifier.size(100.dp),
                                        painter = painterResource(R.drawable.empty_result),
                                        contentDescription = "clear"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
