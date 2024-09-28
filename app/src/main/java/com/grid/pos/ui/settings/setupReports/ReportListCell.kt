package com.grid.pos.ui.settings.setupReports

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.grid.pos.model.FileModel
import com.grid.pos.model.SettingsModel
import com.grid.pos.ui.theme.GridPOSTheme
import com.grid.pos.ui.theme.LightGreen

@Composable
fun ReportListCell(
    modifier: Modifier = Modifier,
    fileModel: FileModel,
    onClick: () -> Unit = {},
) {
    Card(
        modifier = modifier.clickable { onClick.invoke() },
        colors = CardDefaults.cardColors(
            containerColor = Color.White,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = fileModel.getFullName(),
                modifier = Modifier
                    .weight(1f)
                    .wrapContentHeight(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start,
                style = TextStyle(
                    fontWeight = if (fileModel.selected) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 16.sp
                ),
                color = if (fileModel.selected) LightGreen else SettingsModel.textColor
            )
            Spacer(modifier = Modifier.width(5.dp))
        }
        HorizontalDivider(color = Color.LightGray)
    }
}

@Preview(showBackground = true)
@Composable
fun LicenseListCellPreview() {
    GridPOSTheme {
        ReportListCell(
            Modifier,
            FileModel(
                "PaySlip_en",
                "Default",
                true
            )
        )
    }
}