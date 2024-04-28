package com.grid.pos.ui.family

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.grid.pos.data.Family.Family
import com.grid.pos.ui.theme.Blue
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.Grey

@Composable
fun CategoryCell(
    family: Family,
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 5.dp)
            .background(color = Color.Transparent)
    ) {
        Box( // Use a Box to achieve content alignment
            modifier = Modifier
                .width(120.dp)
                .height(80.dp)
                .let {
                    if (selected) {
                        it.background(color = Blue, shape = RoundedCornerShape(15.dp))
                    } else {
                        it.background(color = Grey, shape = RoundedCornerShape(15.dp))
                    }
                },
            contentAlignment = Alignment.Center // Center content within the Box
        ) {
            Text(
                text = family.familyName ?: "BlaBla",
                color = Color.White,
                modifier = Modifier
                    .padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CategoryCellPreview() {
    GridPOSTheme {
        CategoryCell(Family( "1", "Bilal", "1"), true)
    }
}