package com.grid.pos.ui.item

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grid.pos.data.Item.Item
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils

@Composable
fun ItemListCell(
        modifier: Modifier = Modifier,
        items: MutableList<Item>,
        notifyDirectly:Boolean = false,
        onClick: (Item) -> Unit = { _ -> }
) {
    LazyVerticalGrid(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        columns = GridCells.Adaptive(120.dp)
    ) {
        items.forEach { item ->
            item {
                ItemCell(
                    item = item,
                    notifyDirectly=notifyDirectly,
                    modifier = Modifier
                ) {
                    onClick(
                        item
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ItemListCellPreview() {
    GridPOSTheme {
        ItemListCell(
            modifier = Modifier,
            items = mutableListOf(
                Item(
                    itemId = "1",
                    itemName = "Chicken",
                    itemUnitPrice = 100.0
                ),
                Item(
                    itemId = "2",
                    itemName = "Salad",
                    itemUnitPrice = 100.0
                ),
                Item(
                    itemId = "3",
                    itemName = "Veg",
                    itemUnitPrice = 100.0
                ),
                Item(
                    itemId = "4",
                    itemName = "Other",
                    itemUnitPrice = 100.0
                ),
                Item(
                    itemId = "5",
                    itemName = "Other1",
                    itemUnitPrice = 100.0
                ),
                Item(
                    itemId = "6",
                    itemName = "Other2",
                    itemUnitPrice = 100.0
                ),
                Item(
                    itemId = "7",
                    itemName = "Other3",
                    itemUnitPrice = 100.0
                ),
            )
        )
    }
}