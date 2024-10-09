package com.grid.pos.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grid.pos.data.Family.Family
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.utils.Utils

@Composable
fun CategoryListCell(
        categories: MutableList<Family>,
        onClick: (Family) -> Unit = {},
        modifier: Modifier = Modifier
) {
    var selectionState by remember { mutableIntStateOf(-1) }
    LaunchedEffect(categories) {
        if (selectionState == -1 && categories.size > 0) {
            selectionState = 0
            onClick(categories[0])
        }
    }
    ScrollableTabRow(selectedTabIndex = selectionState,
        modifier = modifier.padding(vertical = 5.dp),
        divider = { null },
        edgePadding = 0.dp,
        contentColor = SettingsModel.backgroundColor,
        containerColor = SettingsModel.backgroundColor,
        indicator = {
            null
        }) {
        categories.forEachIndexed { index, category ->
            CategoryCell(
                family = category,
                selected = selectionState == index,
                modifier = Modifier
            ) {
                selectionState = index
                onClick(category)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryListCellPreview() {
    GridPOSTheme {
        CategoryListCell(
            categories = mutableListOf(
                Family(
                    "1",
                    "Chicken"
                ),
                Family(
                    "2",
                    "Meat"
                ),
                Family(
                    "3",
                    "Salad"
                ),
                Family(
                    "4",
                    "Veg"
                ),
                Family(
                    "5",
                    "Other"
                )
            ),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        )
    }
}