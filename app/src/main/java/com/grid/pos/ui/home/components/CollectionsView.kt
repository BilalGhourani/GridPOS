package com.grid.pos.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.grid.pos.ui.family.CategoryListCell
import com.grid.pos.ui.Item.ItemListCell
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.Grey
import com.grid.pos.utils.Utils

@Composable
fun CollectionsView(
    navController: NavController? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        CategoryListCell(categories = Utils.categories)
        Spacer(modifier = Modifier.height(3.dp))
        Divider(modifier = Modifier
            .height(1.dp)
            .background(color = Grey))
        Spacer(modifier = Modifier.height(3.dp))
        ItemListCell(items = Utils.listOfItems)
    }
}

@Preview(showBackground = true)
@Composable
fun CollectionsViewPreview() {
    GridPOSTheme {
        CollectionsView()
    }
}