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
        onClick: (Item) -> Unit = {}
) {
    LazyVerticalGrid(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight(), columns = GridCells.Adaptive(120.dp)
    ) {
        items.forEach { item ->
            item {
                ItemCell(
                    item = item,
                    Modifier,
                ) {
                    onClick(item)
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
            Utils.listOfItems
        )
    }
}