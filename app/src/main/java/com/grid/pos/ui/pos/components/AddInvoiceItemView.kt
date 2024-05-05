package com.grid.pos.ui.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Item.Item
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.Item.ItemListCell
import com.grid.pos.ui.family.CategoryListCell
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils

@Composable
fun AddInvoiceItemView(
    categories: MutableList<Family> = Utils.categories,
    items: MutableList<Item> = Utils.listOfItems,
    modifier: Modifier = Modifier,
    onSelect: (List<Item>) -> Unit = {},
) {
    val itemsState by remember { mutableStateOf(mutableListOf<Item>()) }
    var familyState by remember { mutableStateOf("") }
    LaunchedEffect(true) {
        if (familyState.isEmpty() && categories.size > 0) {
            familyState = categories[0].familyId
        }
    }
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp)
        ) {
            CategoryListCell(
                modifier=Modifier,
                categories = categories,
                onClick = { familyState = it.familyId }
            )
            Spacer(modifier = Modifier.height(3.dp))
            HorizontalDivider(
                modifier = Modifier
                    .height(1.dp)
                    .background(color = Color.LightGray)
            )
            Spacer(modifier = Modifier.height(3.dp))
            val familyItems =
                items.filter { it.itemPos && it.itemFaId.equals(familyState, ignoreCase = true) }

            ItemListCell(items = familyItems.toMutableList(), onClick = { itemsState.add(it) })
        }
        FloatingActionButton(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(30.dp),
            onClick = { onSelect.invoke(itemsState) },
            shape = CircleShape,
            containerColor = SettingsModel.buttonColor,
            contentColor = SettingsModel.buttonTextColor
        ) {
            Icon(Icons.Filled.ArrowBackIosNew, "Submit", modifier = Modifier.rotate(180f))
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