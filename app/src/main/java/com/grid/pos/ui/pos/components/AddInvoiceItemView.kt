package com.grid.pos.ui.pos.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shadowElevation = 3.dp,
            color = Color.White
        ) {
            TextButton(
                modifier = Modifier
                    .wrapContentWidth(align = Alignment.End)
                    .wrapContentHeight(align = Alignment.CenterVertically)
                    .padding(horizontal = 10.dp),
                onClick = { onSelect.invoke(itemsState) }
            ) {
                Text(text = "Done", color = Color.Black)
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        CategoryListCell(
            categories = categories,
            onClick = { familyState = it.familyId }
        )
        Spacer(modifier = Modifier.height(3.dp))
        HorizontalDivider(
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