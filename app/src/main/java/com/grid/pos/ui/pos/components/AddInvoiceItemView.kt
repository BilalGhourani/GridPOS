package com.grid.pos.ui.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Divider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grid.pos.data.Family.Family
import com.grid.pos.data.Item.Item
import com.grid.pos.model.InvoiceItemModel
import com.grid.pos.ui.Item.ItemListCell
import com.grid.pos.ui.family.CategoryListCell
import com.grid.pos.ui.theme.Grey
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils

@Composable
fun AddInvoiceItemView(
    categories: MutableList<Family> = Utils.categories,
    items: MutableList<Item> = Utils.listOfItems,
    modifier: Modifier = Modifier,
    onSelect: (List<Item>) -> Unit = {},
) {

    var itemsState by remember { mutableStateOf(mutableListOf<Item>()) }
    var familyState by remember { mutableStateOf("") }
    LaunchedEffect(true) {
        if (familyState.isEmpty() && categories.size > 0) {
            familyState = categories[0].familyId
        }
    }
    Column(
        modifier = modifier
    ) {
        IconButton(
            modifier = Modifier
                .width(150.dp)
                .height(80.dp)
                .wrapContentHeight(align = Alignment.CenterVertically)
                .padding(horizontal = 10.dp)
                .align(Alignment.End),
            onClick = { onSelect.invoke(itemsState) }
        ) {
            Text(text = "Done", color = Color.Black)
        }
        CategoryListCell(
            categories = categories,
            onClick = { familyState = it.familyId }
        )
        Spacer(modifier = Modifier.height(3.dp))
        Divider(
            modifier = Modifier
                .height(1.dp)
                .background(color = Grey)
        )
        Spacer(modifier = Modifier.height(3.dp))
        val familyItems =
            items.filter { it.itemPos && it.itemFaId.equals(familyState, ignoreCase = true) }

        ItemListCell(items = familyItems.toMutableList(), onClick = { itemsState.add(it) })
    }
}

@Preview(showBackground = true)
@Composable
fun AddInvoiceItemViewPreview() {
    GridPOSTheme {
        AddInvoiceItemView()
    }
}