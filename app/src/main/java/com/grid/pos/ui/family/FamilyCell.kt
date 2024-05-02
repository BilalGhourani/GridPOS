package com.grid.pos.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grid.pos.data.Family.Family
import com.grid.pos.ui.common.UIButton
import com.grid.pos.ui.theme.Blue
import com.grid.pos.ui.theme.Grey
import com.grid.pos.ui.theme.GridPOSTheme

@Composable
fun CategoryCell(
    family: Family,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Box(
        modifier = modifier
            .padding(horizontal = 5.dp)
            .background(color = Color.Transparent)
    ) {
        UIButton(
            modifier = modifier
                .wrapContentWidth()
                .height(80.dp)
                .padding(horizontal = 5.dp)
                .let {
                    if (selected) {
                        it.background(color = Blue, shape = RoundedCornerShape(15.dp))
                    } else {
                        it.background(color = Grey, shape = RoundedCornerShape(15.dp))
                    }
                },
            text = family.familyName ?: "N/A"
        ) {
            onClick.invoke()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryCellPreview() {
    GridPOSTheme {
        CategoryCell(Family("1", "Bilal", "1"), true)
    }
}