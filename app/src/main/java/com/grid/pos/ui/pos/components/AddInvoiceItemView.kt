package com.grid.pos.ui.pos.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.pos.ActivityScopedViewModel
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Item.Item
import com.grid.pos.model.PopupModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.family.CategoryListCell
import com.grid.pos.ui.item.ItemListCell
import com.grid.pos.ui.theme.GridPOSTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AddInvoiceItemView(
        activityViewModel: ActivityScopedViewModel? = null,
        categories: MutableList<Family> = mutableListOf(),
        items: MutableList<Item> = mutableListOf(),
        modifier: Modifier = Modifier,
        notifyDirectly: Boolean = false,
        onSelect: (List<Item>) -> Unit = {},
) {
    val itemsState = remember { mutableStateListOf<Item>() }
    var familyState by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    fun saveAndBack() {
        scope.launch(Dispatchers.IO) {
            itemsState.forEach { item ->
                item.selected = false
            }
        }
        onSelect.invoke(itemsState.toMutableList())
    }
    if (!notifyDirectly) {
        BackHandler {
            saveAndBack()
        }
    }

    LaunchedEffect(true) {
        if (familyState.isEmpty() && categories.size > 0) {
            familyState = categories[0].familyId
        }
    }
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = 10.dp,
                    top = 10.dp,
                    end = 10.dp
                )
        ) {
            CategoryListCell(modifier = Modifier,
                categories = categories,
                onClick = { familyState = it.familyId })
            Spacer(modifier = Modifier.height(3.dp))
            HorizontalDivider(
                modifier = Modifier
                    .height(1.dp)
                    .background(color = Color.LightGray)
            )
            Spacer(modifier = Modifier.height(3.dp))
            val familyItems = items.filter {
                it.itemPos && it.itemFaId.equals(
                    familyState,
                    ignoreCase = true
                )
            }

            ItemListCell(items = familyItems.toMutableList(),
                notifyDirectly = notifyDirectly,
                onClick = { item ->
                    if (item.itemOpenQty <= 0) {
                        item.selected = false
                        if (SettingsModel.showItemQtyAlert) {
                            activityViewModel?.showPopup(
                                true,
                                PopupModel(
                                    dialogText = "Not enough stock available for ${item.itemName}. Please adjust the quantity.",
                                    positiveBtnText = "Close",
                                    negativeBtnText = null
                                )
                            )
                        }
                    } else {
                        if (notifyDirectly) {
                            onSelect.invoke(listOf(item))
                        } else {
                            if (item.selected) {
                                itemsState.add(item)
                            } else {
                                itemsState.remove(item)
                            }
                        }
                    }
                })
        }
        if (!notifyDirectly && itemsState.isNotEmpty()) {
            FloatingActionButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(30.dp),
                onClick = {
                    saveAndBack()
                },
                shape = CircleShape,
                containerColor = SettingsModel.buttonColor,
                contentColor = SettingsModel.buttonTextColor
            ) {
                Text(
                    text = "${itemsState.size}",
                    modifier = Modifier.wrapContentWidth(Alignment.CenterHorizontally),
                    color = Color.White,
                    style = TextStyle(
                        textDecoration = TextDecoration.None,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    textAlign = TextAlign.Center,
                )/*Icon(
                    Icons.Filled.ArrowBackIosNew,
                    "Submit",
                    modifier = Modifier.rotate(180f)
                )*/
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddInvoiceItemViewPreview() {
    GridPOSTheme {
        AddInvoiceItemView()
    }
}